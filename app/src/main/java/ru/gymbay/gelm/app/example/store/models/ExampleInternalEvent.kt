package ru.gymbay.gelm.app.example.store.models

sealed interface ExampleInternalEvent {
    data class LoadedData(val list: List<String>) : ExampleInternalEvent
}