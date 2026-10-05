package com.machinahnos.vehiculos

/**
 * Biblioteca de identidad de modelos.
 * Separa la personalidad con la que fue concebido un modelo de los datos
 * particulares de una unidad usada (año, km, precio, etc.).
 *
 * La biblioteca puede crecer sin tocar el motor de recomendación.
 */
data class ModelIdentity(
    val brand: String,
    val modelTokens: List<String>,
    val dna: Set<VehicleDNA>,
    val typicalUses: Set<UseCase> = emptySet(),
    val strengths: Set<Priority> = emptySet()
)

object VehicleModelLibrary {
    private val identities = listOf(
        ModelIdentity("Peugeot", listOf("208"), setOf(VehicleDNA.URBANO, VehicleDNA.JOVEN, VehicleDNA.TECNOLOGICO), setOf(UseCase.CIUDAD, UseCase.MIXTO), setOf(Priority.ECONOMIA, Priority.TECNOLOGIA, Priority.PRESENCIA)),
        ModelIdentity("Toyota", listOf("Corolla"), setOf(VehicleDNA.RACIONAL, VehicleDNA.FAMILIAR, VehicleDNA.ELEGANTE), setOf(UseCase.CIUDAD, UseCase.FAMILIA, UseCase.RUTA), setOf(Priority.CONFIABILIDAD, Priority.CONFORT, Priority.SEGURIDAD)),
        ModelIdentity("Volkswagen", listOf("Amarok"), setOf(VehicleDNA.ROBUSTO, VehicleDNA.TRABAJADOR, VehicleDNA.PREMIUM), setOf(UseCase.TRABAJO, UseCase.CARGA, UseCase.CAMPO, UseCase.RUTA), setOf(Priority.PRESTACIONES, Priority.ESPACIO, Priority.PRESENCIA)),
        ModelIdentity("Jeep", listOf("Renegade"), setOf(VehicleDNA.AVENTURERO, VehicleDNA.URBANO, VehicleDNA.JOVEN), setOf(UseCase.CIUDAD, UseCase.FAMILIA, UseCase.MIXTO), setOf(Priority.PRESENCIA, Priority.CONFORT, Priority.ESPACIO)),
        ModelIdentity("Chevrolet", listOf("Cruze"), setOf(VehicleDNA.TECNOLOGICO, VehicleDNA.ELEGANTE, VehicleDNA.DEPORTIVO), setOf(UseCase.CIUDAD, UseCase.FAMILIA, UseCase.RUTA), setOf(Priority.TECNOLOGIA, Priority.CONFORT, Priority.PRESTACIONES)),
        ModelIdentity("Ford", listOf("Territory"), setOf(VehicleDNA.FAMILIAR, VehicleDNA.TECNOLOGICO, VehicleDNA.PREMIUM), setOf(UseCase.FAMILIA, UseCase.CIUDAD, UseCase.RUTA), setOf(Priority.TECNOLOGIA, Priority.CONFORT, Priority.ESPACIO))
    )

    fun find(brand: String, model: String): ModelIdentity? {
        val normalizedBrand = normalize(brand)
        val normalizedModel = normalize(model)
        return identities.firstOrNull { identity ->
            normalize(identity.brand) == normalizedBrand &&
                identity.modelTokens.any { normalizedModel.contains(normalize(it)) }
        }
    }

    private fun normalize(value: String): String = value
        .lowercase()
        .replace("á", "a").replace("é", "e").replace("í", "i")
        .replace("ó", "o").replace("ú", "u").trim()
}

object VehicleClassifier {
    /**
     * Enriquece una unidad cargada en stock con el ADN conocido de su modelo.
     * Nunca pisa información específica ya cargada: la complementa.
     */
    fun classify(vehicle: Vehicle): Vehicle {
        val identity = VehicleModelLibrary.find(vehicle.brand, vehicle.model) ?: return inferFromVehicle(vehicle)
        return vehicle.copy(
            dna = vehicle.dna + identity.dna,
            uses = vehicle.uses + identity.typicalUses,
            strengths = vehicle.strengths + identity.strengths
        )
    }

    fun classifyStock(stock: List<Vehicle>): List<Vehicle> = stock.map(::classify)

    /** Fallback cuando todavía no conocemos el modelo en la biblioteca. */
    private fun inferFromVehicle(vehicle: Vehicle): Vehicle {
        val inferredDna = buildSet {
            when (vehicle.body) {
                BodyType.PICKUP, BodyType.UTILITARIO, BodyType.VAN -> add(VehicleDNA.TRABAJADOR)
                BodyType.HATCHBACK -> add(VehicleDNA.URBANO)
                BodyType.SUV -> add(VehicleDNA.FAMILIAR)
                BodyType.COUPE -> add(VehicleDNA.DEPORTIVO)
                BodyType.SEDAN, BodyType.WAGON -> add(VehicleDNA.RACIONAL)
                BodyType.OTHER -> Unit
            }
            if (vehicle.drive == Drive.FOUR_X_FOUR || vehicle.drive == Drive.AWD) {
                add(VehicleDNA.AVENTURERO)
                add(VehicleDNA.ROBUSTO)
            }
            if (Priority.TECNOLOGIA in vehicle.strengths) add(VehicleDNA.TECNOLOGICO)
            if (Priority.PRESENCIA in vehicle.strengths) add(VehicleDNA.ELEGANTE)
        }
        return vehicle.copy(dna = vehicle.dna + inferredDna)
    }
}
