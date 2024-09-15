package io.github.gymbay.gelm.app.example.store.models

sealed interface ExampleCommand {
    data class StartLoading(val text: String) : ExampleCommand
}