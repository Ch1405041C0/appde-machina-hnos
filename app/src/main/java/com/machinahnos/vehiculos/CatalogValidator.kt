package com.machinahnos.vehiculos

data class CatalogIssue(
    val vehicleId: String?,
    val field: String,
    val message: String
)

data class CatalogValidation(
    val issues: List<CatalogIssue>
) {
    val isValid: Boolean get() = issues.isEmpty()

    fun requireValid() {
        require(isValid) {
            issues.joinToString(prefix = "invalid vehicle catalog: ", separator = "; ") {
                "${it.vehicleId ?: "catalog"}.${it.field}: ${it.message}"
            }
        }
    }
}

/**
 * Boundary between externally supplied stock and the recommendation engine.
 * Invalid or duplicated units must be rejected before they can shape questions
 * or recommendations.
 */
object CatalogValidator {
    fun validate(stock: List<Vehicle>): CatalogValidation {
        val issues = mutableListOf<CatalogIssue>()
        val ids = mutableSetOf<String>()

        stock.forEach { vehicle ->
            if (vehicle.id.isBlank()) {
                issues += CatalogIssue(null, "id", "must not be blank")
            } else if (!ids.add(vehicle.id)) {
                issues += CatalogIssue(vehicle.id, "id", "must be unique")
            }
            if (vehicle.brand.isBlank()) issues += CatalogIssue(vehicle.id, "brand", "must not be blank")
            if (vehicle.model.isBlank()) issues += CatalogIssue(vehicle.id, "model", "must not be blank")
            if (vehicle.year !in 1990..2100) issues += CatalogIssue(vehicle.id, "year", "must be between 1990 and 2100")
            if (vehicle.kilometers < 0) issues += CatalogIssue(vehicle.id, "kilometers", "must be zero or greater")
            if (vehicle.price < 0) issues += CatalogIssue(vehicle.id, "price", "must be zero or greater")
            if (vehicle.seats <= 0) issues += CatalogIssue(vehicle.id, "seats", "must be greater than zero")
        }

        return CatalogValidation(issues)
    }

    fun prepare(stock: List<Vehicle>): List<Vehicle> {
        validate(stock).requireValid()
        return VehicleClassifier.classifyStock(stock)
    }
}
