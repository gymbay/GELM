package io.github.gymbay.gelm.app.example.store.components

import io.github.gymbay.gelm.GelmActor
import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ExampleActor : GelmActor<ExampleCommand, ExampleInternalEvent>() {
    override suspend fun execute(command: ExampleCommand): Flow<ExampleInternalEvent> = flow {
        when (command) {
            is ExampleCommand.StartLoading -> {
                delay(3.seconds)
                val list = mutableListOf<String>()
                for (i in 1..Random.nextInt(1, 100)) {
                    list.add("${command.text} N $i")
                }
                emit(ExampleInternalEvent.LoadedData(list))
            }
        }
    }
}