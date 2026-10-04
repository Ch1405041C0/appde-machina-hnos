package com.machinahnos.vehiculos

/**
 * Regla madre de Machina:
 * EL STOCK DEFINE LAS PREGUNTAS. LAS RESPUESTAS DEFINEN LA RECOMENDACION.
 *
 * Este motor no usa IA. Clasifica stock, genera solo preguntas que pueden
 * diferenciar unidades disponibles y calcula un ranking explicable.
 */

enum class BodyType { HATCHBACK, SEDAN, SUV, PICKUP, WAGON, COUPE, VAN, UTILITARIO, OTHER }
enum class Fuel { NAFTA, DIESEL, GNC, HIBRIDO, ELECTRICO, OTHER }
enum class Gearbox { MANUAL, AUTOMATICA, OTHER }
enum class Drive { FWD, RWD, AWD, FOUR_X_FOUR, OTHER }
enum class UseCase { CIUDAD, FAMILIA, RUTA, TRABAJO, CARGA, CAMPO, MIXTO }
enum class Priority { ECONOMIA, CONFIABILIDAD, CONFORT, SEGURIDAD, TECNOLOGIA, ESPACIO, PRESTACIONES, PRESENCIA }

data class Vehicle(
    val id: String,
    val brand: String,
    val model: String,
    val year: Int,
    val kilometers: Int,
    val price: Long,
    val body: BodyType,
    val fuel: Fuel,
    val gearbox: Gearbox,
    val drive: Drive = Drive.OTHER,
    val seats: Int = 5,
    val uses: Set<UseCase> = emptySet(),
    val strengths: Set<Priority> = emptySet()
) {
    fun kmPerYear(currentYear: Int = 2026): Int {
        val age = (currentYear - year).coerceAtLeast(1)
        return kilometers / age
    }
}

data class BuyerProfile(
    val useCase: UseCase? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val maxPrice: Long? = null,
    val fuel: Fuel? = null,
    val gearbox: Gearbox? = null,
    val bodyPreference: BodyType? = null,
    val needs4x4: Boolean? = null,
    val minSeats: Int? = null,
    val priority: Priority? = null,
    val wantsLowKm: Boolean? = null
)

data class Question(
    val id: String,
    val text: String,
    val options: List<String>
)

data class Recommendation(
    val vehicle: Vehicle,
    val score: Int,
    val reasons: List<String>
)

object DynamicQuestionEngine {
    fun questions(stock: List<Vehicle>): List<Question> {
        if (stock.isEmpty()) return emptyList()
        val result = mutableListOf<Question>()

        val uses = stock.flatMap { it.uses }.distinct()
        if (uses.size > 1) result += Question(
            "use", "¿Para qué lo vas a usar principalmente?",
            uses.map { it.label() } + "Un poco de todo"
        )

        val years = stock.map { it.year }.distinct().sorted()
        if (years.size > 1) result += Question(
            "year", "¿Qué antigüedad te resulta cómoda?",
            yearOptions(years)
        )

        val fuels = stock.map { it.fuel }.distinct()
        if (fuels.size > 1) result += Question(
            "fuel", "¿Tenés alguna preferencia de combustible?",
            fuels.map { it.label() } + "Me da igual"
        )

        val gearboxes = stock.map { it.gearbox }.distinct()
        if (gearboxes.size > 1) result += Question(
            "gearbox", "¿Cómo preferís manejar?",
            gearboxes.map { it.label() } + "Me da igual"
        )

        val bodies = stock.map { it.body }.distinct()
        if (bodies.size > 1) result += Question(
            "body", "¿Hay algún estilo de auto que te guste más?",
            bodies.map { it.label() } + "Sorprendeme"
        )

        if (stock.any { it.drive == Drive.FOUR_X_FOUR || it.drive == Drive.AWD } &&
            stock.any { it.drive != Drive.FOUR_X_FOUR && it.drive != Drive.AWD }) {
            result += Question(
                "terrain", "¿Vas a usarlo seguido en campo, barro o caminos complicados?",
                listOf("Sí, necesito buena tracción", "A veces", "Casi nunca")
            )
        }

        if (stock.map { it.seats }.distinct().size > 1) result += Question(
            "seats", "¿Cuántas personas viajan habitualmente?",
            listOf("1 o 2", "3 o 4", "5 o más")
        )

        if (stock.map { kmBand(it) }.distinct().size > 1) result += Question(
            "km", "¿Qué importancia tiene para vos el kilometraje?",
            listOf("Quiero pocos km", "Busco equilibrio precio/km", "No me importa si está bien cuidado")
        )

        val strengths = stock.flatMap { it.strengths }.distinct()
        if (strengths.size > 1) result += Question(
            "priority", "¿Qué te hace decir ‘este auto es para mí’?",
            strengths.map { it.label() } + "Un buen equilibrio"
        )

        return result
    }

    private fun yearOptions(years: List<Int>): List<String> {
        val min = years.first()
        val max = years.last()
        val middle = years[years.size / 2]
        return listOf("Desde $middle en adelante", "Entre $min y $max", "El año no es decisivo")
    }

    private fun kmBand(v: Vehicle): String = when {
        v.kilometers <= 40_000 -> "LOW"
        v.kmPerYear() <= 10_000 -> "LOW_USE"
        v.kmPerYear() >= 25_000 -> "HIGH_USE"
        else -> "MEDIUM"
    }
}

object RecommendationEngine {
    fun recommend(stock: List<Vehicle>, profile: BuyerProfile, limit: Int = 3): List<Recommendation> {
        return stock
            .filter { hardFilters(it, profile) }
            .map { score(it, profile) }
            .sortedByDescending { it.score }
            .take(limit)
    }

    private fun hardFilters(v: Vehicle, p: BuyerProfile): Boolean {
        if (p.maxPrice != null && v.price > p.maxPrice) return false
        if (p.minYear != null && v.year < p.minYear) return false
        if (p.maxYear != null && v.year > p.maxYear) return false
        if (p.minSeats != null && v.seats < p.minSeats) return false
        if (p.needs4x4 == true && v.drive != Drive.FOUR_X_FOUR && v.drive != Drive.AWD) return false
        return true
    }

    private fun score(v: Vehicle, p: BuyerProfile): Recommendation {
        var score = 50
        val reasons = mutableListOf<String>()

        if (p.useCase != null && p.useCase in v.uses) {
            score += 18; reasons += "encaja con el uso que le vas a dar"
        }
        if (p.fuel != null && p.fuel == v.fuel) {
            score += 10; reasons += "usa el combustible que preferís"
        }
        if (p.gearbox != null && p.gearbox == v.gearbox) {
            score += 12; reasons += "tiene la caja que buscás"
        }
        if (p.bodyPreference != null && p.bodyPreference == v.body) {
            score += 8; reasons += "coincide con el estilo que te gusta"
        }
        if (p.priority != null && p.priority in v.strengths) {
            score += 15; reasons += "se destaca especialmente en ${p.priority.label().lowercase()}"
        }
        if (p.wantsLowKm == true) {
            when {
                v.kilometers <= 40_000 -> { score += 12; reasons += "tiene kilometraje bajo" }
                v.kmPerYear() <= 10_000 -> { score += 8; reasons += "tuvo poco uso anual para su edad" }
                v.kmPerYear() >= 25_000 -> score -= 10
            }
        }

        return Recommendation(v, score.coerceIn(0, 100), reasons)
    }
}

private fun UseCase.label() = when (this) {
    UseCase.CIUDAD -> "Ciudad / todos los días"
    UseCase.FAMILIA -> "Familia"
    UseCase.RUTA -> "Ruta / viajes"
    UseCase.TRABAJO -> "Trabajo"
    UseCase.CARGA -> "Carga / reparto"
    UseCase.CAMPO -> "Campo"
    UseCase.MIXTO -> "Uso mixto"
}

private fun Fuel.label() = when (this) {
    Fuel.NAFTA -> "Nafta"; Fuel.DIESEL -> "Diésel"; Fuel.GNC -> "GNC"
    Fuel.HIBRIDO -> "Híbrido"; Fuel.ELECTRICO -> "Eléctrico"; Fuel.OTHER -> "Otro"
}

private fun Gearbox.label() = when (this) {
    Gearbox.MANUAL -> "Manual"; Gearbox.AUTOMATICA -> "Automático"; Gearbox.OTHER -> "Otra"
}

private fun BodyType.label() = when (this) {
    BodyType.HATCHBACK -> "Hatchback"; BodyType.SEDAN -> "Sedán"; BodyType.SUV -> "SUV"
    BodyType.PICKUP -> "Pickup"; BodyType.WAGON -> "Rural / familiar"; BodyType.COUPE -> "Coupé"
    BodyType.VAN -> "Van"; BodyType.UTILITARIO -> "Utilitario"; BodyType.OTHER -> "Otro"
}

private fun Priority.label() = when (this) {
    Priority.ECONOMIA -> "Economía"; Priority.CONFIABILIDAD -> "Confiabilidad"
    Priority.CONFORT -> "Comodidad"; Priority.SEGURIDAD -> "Seguridad"
    Priority.TECNOLOGIA -> "Tecnología"; Priority.ESPACIO -> "Espacio"
    Priority.PRESTACIONES -> "Respuesta / prestaciones"; Priority.PRESENCIA -> "Presencia / diseño"
}
