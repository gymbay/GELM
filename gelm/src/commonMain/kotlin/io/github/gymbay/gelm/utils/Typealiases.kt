package io.github.gymbay.gelm.utils

import io.github.gymbay.gelm.GelmStore

/**
 * Convenience alias for [io.github.gymbay.gelm.GelmStore] with four type parameters and
 * `InternalEvent = Nothing`.
 *
 * The alias can be used as a supertype:
 * ```
 * import io.github.gymbay.gelm.utils.GelmStore
 *
 * class LoginStore : GelmStore<LoginState, LoginEffect, LoginEvent, LoginCommand>(
 *     initialState = LoginState(),
 *     externalReducer = LoginExternalReducer(),
 * )
 * ```
 *
 * It expands to [io.github.gymbay.gelm.GelmStore] with `InternalEvent = Nothing`, so it is
 * suitable for stores without internal events. Use the five-type-parameter class when an actor
 * and internal reducer are needed.
 *
 * The alias exists only at compile time and does not create a new runtime class: all stores
 * declared directly through it are the same `GelmStore` class at runtime. If a store needs a
 * distinct ViewModel or dependency-injection key, declare a concrete named subclass. A generic
 * application base class also keeps a single runtime class, so declare one non-generic concrete
 * class per store.
 */
typealias GelmStore<State, Effect, Event, Command> = GelmStore<State, Effect, Event, Nothing, Command>
