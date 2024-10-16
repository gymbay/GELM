package io.github.gymbay.gelm.app.example.store.components

import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleState
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExampleExternalReducerTest : TestCase() {

    @Test
    fun testReloadEvent() {
        val inputText = "Text"

        val reducer = ExampleExternalReducer()
        val result = reducer.startProcessing(
            state = ExampleState(editField = inputText),
            event = ExampleEvent.Reload
        )

        // state
        assertEquals(
            ExampleState(
                editField = inputText,
                isLoading = true
            ),
            result.state
        )
        // effects
        assertTrue(result.effects.isEmpty())
        // commands
        assertTrue(result.commands.size == 1)
        assertEquals(
            ExampleCommand.StartLoading(text = inputText),
            result.commands.first()
        )
        // cancelled commands
        assertTrue(result.cancelledCommands.isEmpty())
        // observer events
        assertTrue(result.observersEvents.isEmpty())
    }

}