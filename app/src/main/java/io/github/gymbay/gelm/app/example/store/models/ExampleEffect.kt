package io.github.gymbay.gelm.app.example.store.models

sealed interface ExampleEffect {
    data object NavigateToScreen : ExampleEffect
}