package com.machinahnos.vehiculos

/**
 * Identidad comercial de un modelo, separada de los datos de cada unidad usada.
 */
data class ModelIdentity(
    val brand: String,
    val modelTokens: List<String>,
    val dna: Set<VehicleDNA>,
    val typicalUses: Set<UseCase> = emptySet(),
    val strengths: Set<Priority> = emptySet()
)

/**
 * Catálogo reemplazable de identidades.
 *
 * El motor de clasificación no conoce de dónde llegan los datos. Hoy existe un
 * catálogo inicial; mañana puede cargarse desde assets, una API o el panel de
 * stock sin reescribir RecommendationEngine ni VehicleClassifier.
 */
object VehicleModelLibrary {
    private val defaultIdentities = listOf(
        ModelIdentity("Peugeot", listOf("208"), setOf(VehicleDNA.URBANO, VehicleDNA.JOVEN, VehicleDNA.TECNOLOGICO), setOf(UseCase.CIUDAD, UseCase.MIXTO), setOf(Priority.ECONOMIA, Priority.TECNOLOGIA, Priority.PRESENCIA)),
        ModelIdentity("Toyota", listOf("Corolla"), setOf(VehicleDNA.RACIONAL, VehicleDNA.FAMILIAR, VehicleDNA.ELEGANTE), setOf(UseCase.CIUDAD, UseCase.FAMILIA, UseCase.RUTA), setOf(Priority.CONFIABILIDAD, Priority.CONFORT, Priority.SEGURIDAD)),
        ModelIdentity("Volkswagen", listOf("Amarok"), setOf(VehicleDNA.ROBUSTO, VehicleDNA.TRABAJADOR, VehicleDNA.PREMIUM), setOf(UseCase.TRABAJO, UseCase.CARGA, UseCase.CAMPO, UseCase.RUTA), setOf(Priority.PRESTACIONES, Priority.ESPACIO, Priority.PRESENCIA)),
        ModelIdentity("Jeep", listOf("Renegade"), setOf(VehicleDNA.AVENTURERO, VehicleDNA.URBANO, VehicleDNA.JOVEN), setOf(UseCase.CIUDAD, UseCase.FAMILIA, UseCase.MIXTO), setOf(Priority.PRESENCIA, Priority.CONFORT, Priority.ESPACIO)),
        ModelIdentity("Chevrolet", listOf("Cruze"), setOf(VehicleDNA.TECNOLOGICO, VehicleDNA.ELEGANTE, VehicleDNA.DEPORTIVO), setOf(UseCase.CIUDAD, UseCase.FAMILIA, UseCase.RUTA), setOf(Priority.TECNOLOGIA, Priority.CONFORT, Priority.PRESTACIONES)),
        ModelIdentity("Ford", listOf("Territory"), setOf(VehicleDNA.FAMILIAR, VehicleDNA.TECNOLOGICO, VehicleDNA.PREMIUM), setOf(UseCase.FAMILIA, UseCase.CIUDAD, UseCase.RUTA), setOf(Priority.TECNOLOGIA, Priority.CONFORT, Priority.ESPACIO))
    )

    @Volatile
    private var identities: List<ModelIdentity> = defaultIdentities

    /** Reemplaza el catálogo activo con datos validados por la capa que los cargue. */
    fun configure(catalog: List<ModelIdentity>) {
        require(catalog.isNotEmpty()) { "model identity catalog cannot be empty" }
        require(catalog.all { it.brand.isNotBlank() && it.modelTokens.any(String::isNotBlank) }) {
            "every model identity needs a brand and at least one model token"
        }
        identities = catalog.toList()
    }

    /** Vuelve al catálogo incluido en la app; útil para modo offline y pruebas. */
    fun resetToDefaults() {
        identities = defaultIdentities
    }

    fun snapshot(): List<ModelIdentity> = identities.toList()

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
    fun classify(vehicle: Vehicle): Vehicle {
        val identity = VehicleModelLibrary.find(vehicle.brand, vehicle.model) ?: return inferFromVehicle(vehicle)
        return vehicle.copy(
            dna = vehicle.dna + identity.dna,
            uses = vehicle.uses + identity.typicalUses,
            strengths = vehicle.strengths + identity.strengths
        )
    }

    fun classifyStock(stock: List<Vehicle>): List<Vehicle> = stock.map(::classify)

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
