package ru.gymbay.gelm.app.example.compose.store.models

data class ComposeState(
    val isLoading: Boolean = false,
    val title: String = "Title",
    val editField: String = "default",
    val items: List<String> = emptyList()
)
