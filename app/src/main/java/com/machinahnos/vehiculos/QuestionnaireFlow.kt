package com.machinahnos.vehiculos

/**
 * Estado reversible del cuestionario de recomendación.
 * Las respuestas viven fuera de la pantalla para poder volver, editar y recalcular.
 */
data class QuestionnaireFlow(
    val questions: List<Question>,
    val answers: Map<String, String> = emptyMap(),
    val index: Int = 0,
) {
    val current: Question? get() = questions.getOrNull(index)
    val isFirst: Boolean get() = index == 0
    val isLast: Boolean get() = questions.isNotEmpty() && index == questions.lastIndex
    val progress: Float get() = if (questions.isEmpty()) 1f else (index + 1).toFloat() / questions.size

    fun answer(value: String): QuestionnaireFlow {
        val question = current ?: return this
        return copy(answers = answers + (question.id to value))
    }

    fun next(): QuestionnaireFlow =
        if (index < questions.lastIndex) copy(index = index + 1) else this

    fun previous(): QuestionnaireFlow =
        if (index > 0) copy(index = index - 1) else this

    fun edit(questionId: String): QuestionnaireFlow {
        val target = questions.indexOfFirst { it.id == questionId }
        return if (target >= 0) copy(index = target) else this
    }

    companion object {
        fun forStock(stock: List<Vehicle>): QuestionnaireFlow =
            QuestionnaireFlow(DynamicQuestionEngine.questions(stock))
    }
}
