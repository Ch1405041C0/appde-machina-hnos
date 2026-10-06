package com.machinahnos.vehiculos

/**
 * Punto de entrada del flujo de recomendación.
 * El stock disponible define las preguntas y es también el único universo
 * sobre el que se generan recomendaciones.
 */
data class RecommendationSession(
    val stock: List<Vehicle>,
    val questions: List<Question>,
) {
    val availableUnits: Int get() = stock.size

    fun recommend(profile: BuyerProfile, limit: Int = 3): List<Recommendation> =
        RecommendationEngine.recommend(stock, profile, limit)
}

object RecommendationSessionFactory {
    fun fromStock(rawStock: List<Vehicle>): RecommendationSession {
        val preparedStock = CatalogValidator.prepare(rawStock)
        return RecommendationSession(
            stock = preparedStock,
            questions = DynamicQuestionEngine.questions(preparedStock),
        )
    }
}
