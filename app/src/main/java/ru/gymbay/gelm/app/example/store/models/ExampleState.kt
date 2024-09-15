package ru.gymbay.gelm.app.example.store.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExampleState(
    val isLoading: Boolean = false,
    val title: String = "Title",
    val editField: String = "default",
    val items: List<String> = emptyList()
) : Parcelable
