package ru.gymbay.gelm.app.example.compose.store.components

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.gymbay.gelm.GelmActor
import ru.gymbay.gelm.app.example.compose.store.models.ComposeCommand
import ru.gymbay.gelm.app.example.compose.store.models.ComposeInternalEvent
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ComposeActor : GelmActor<ComposeCommand, ComposeInternalEvent>() {
    override suspend fun execute(command: ComposeCommand): Flow<ComposeInternalEvent> = flow {
        when (command) {
            is ComposeCommand.StartLoading -> {
                delay(3.seconds)
                val list = mutableListOf<String>()
                for (i in 1..Random.nextInt(1, 100)) {
                    list.add("${command.text} N $i")
                }
                emit(ComposeInternalEvent.LoadedData(list))
            }
        }
    }
}