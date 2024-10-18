package io.github.gymbay.gelm.app.example.store.models

sealed interface ExampleStateMachine {
    data class Idle(
        // For simplify Payload can be a separate class
        val title: String,
        val editField: String,
        val items: List<String>
    ) : ExampleStateMachine {
        companion object {
            fun initial() = Idle(
                title = "Title",
                editField = "default",
                items = emptyList()
            )
        }
    }

    data class Fetching(
        // Payload
        val title: String,
        val editField: String
    ) : ExampleStateMachine
}