package io.github.gymbay.gelm.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Устанавливает Main dispatcher для теста и сбрасывает после выполнения блока.
 * Используется в commonTest для тестирования ViewModel/GelmStore на всех платформах.
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun withMainDispatcher(
    dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    block: suspend () -> Unit
) {
    Dispatchers.setMain(dispatcher)
    try {
        block()
    } finally {
        Dispatchers.resetMain()
    }
}
