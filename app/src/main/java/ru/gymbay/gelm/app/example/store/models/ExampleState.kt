package ru.gymbay.gelm.app.example.store.models

data class ExampleState(
    val isLoading: Boolean = false,
    val title: String = "Title",
    val editField: String = "default",
    val items: List<String> = emptyList()
)
