package io.github.gymbay.gelm.utils

import io.github.gymbay.gelm.GelmStore

typealias GelmStore<State, Effect, Event, Command> = GelmStore<State, Effect, Event, Nothing, Command>