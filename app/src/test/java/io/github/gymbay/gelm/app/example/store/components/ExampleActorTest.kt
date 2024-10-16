package io.github.gymbay.gelm.app.example.store.components

import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import junit.framework.TestCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class ExampleActorTest : TestCase() {

    fun testStartLoading() = runTest {
        val inputText = "test"
        val sut = ExampleActor()

        val events = mutableListOf<ExampleInternalEvent>()
        sut.execute(ExampleCommand.StartLoading(text = inputText)).toList(events)

        assertEquals(1, events.size)
        val items = (events[0] as ExampleInternalEvent.LoadedData).list
        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.contains(inputText) })
    }

}