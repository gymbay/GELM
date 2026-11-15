# Design

## Context

See `proposal.md` for motivation and `specs/gelm-store/spec.md` for requirements.

`GelmStore` is an open KMP class with five type parameters that extends
`androidx.lifecycle.ViewModel`. The library also exposes a four-type-parameter typealias in
`io.github.gymbay.gelm.utils`. Because typealiases are compile-time aliases, every store declared
directly through one collapses to the same runtime class.

Kotlin allows type aliases in supertype position (verified with the project's Kotlin 2.1.21
compiler), so the four-type-parameter alias can be used directly as a superclass. It expands to the
five-type-parameter class with `InternalEvent = Nothing`.

Both affected frameworks key by runtime class:

- Koin keys definitions by `KClass` plus qualifier.
- `ViewModelProvider` derives its default key from `KClass.canonicalName`.

As a result, two typealias-based stores are indistinguishable. Koin definitions collide, and a
single `ViewModelStore` can return the already-created store for the wrong alias.

The published JVM metadata currently exposes `lifecycle-viewmodel` and `coroutines-core` as
runtime-only dependencies because `commonMain` declares them with `implementation`. `ViewModel` is a
public supertype, and `Flow`, `StateFlow`, and `CoroutineDispatcher` appear in the public API, so a
consumer compiling a subclass may not have those types on its compile classpath.

## Goals / Non-Goals

**Goals:**

- Allow application code to declare concrete named store classes with no changes to store behavior.
- Support inheritance through both the four-type-parameter alias and the five-type-parameter class.
- Give each concrete store subclass a distinct runtime identity that Koin and Navigation 3 can
  distinguish.
- Preserve direct construction, the four-parameter typealias, and all existing runtime behavior.
- Ensure consumers can compile subclasses with GELM's public API dependencies available at compile
  time.
- Verify the mechanism with tests that mirror the Navigation 3 `ViewModelProvider` lookup.

**Non-Goals:**

- Adding overridable lifecycle, event, or reducer hooks to stores.
- Adding a four-type-parameter convenience base class.
- Changing the constructor signature, the typealias, reducers, actor, or observer APIs.
- Adding Koin or Navigation 3 as library dependencies.
- Changing store concurrency, effect replay, saved-state, or command semantics.

## Decisions

### Open the existing class instead of introducing a new abstraction

`GelmStore` becomes `open` while keeping its existing constructor and public API. Subclasses supply
either the five type parameters of the class or the four type parameters of the alias, and call the
existing constructor.

Alternatives considered:

- **Interface plus delegation**: rejected. A delegated `GelmStore` is itself a `ViewModel` with its
  own `viewModelScope`; the host ViewModel would not manage its lifecycle, creating leak and
  cancellation risks. Avoiding that requires a larger refactor that separates store logic from
  `ViewModel`.
- **Rename and split into four- and five-parameter classes**: rejected. It would break existing
  imports and is not needed to solve runtime type identity.
- **Replace the typealias with a four-parameter open class**: rejected for now. It adds a second
  public `GelmStore` symbol and changes how existing alias users are keyed by DI, with no
  requirement driving it.

Making the class `open` is additive and does not change runtime behavior for existing users.

### Keep store behavior closed

Only the class becomes open. `sendEvent`, `state`, `effect`, and reducer orchestration remain
non-overridable. The required use case is nominal typing for DI and navigation; opening behavior
would let subclasses bypass the mutex, command bookkeeping, and saved-state handling.

These are implementation details, but the spec captures the observable contract: inheritance adds a
type identity only.

### Keep the typealias and direct construction

The four-parameter typealias remains for convenient typing, construction, and inheritance. It
expands to the five-parameter class with `InternalEvent = Nothing`, so it is the preferred base for
stores without internal events. Use the five-parameter class when an actor and internal reducer are
needed.

The typealias itself does not create a distinct runtime type. Concrete subclasses are required for
distinct runtime identity. A generic application base class such as
`class BaseStore<T> : GelmStore<...>` still collapses to one runtime class, so documentation must
warn against it.

### Promote public API dependencies to `api`

`lifecycle-viewmodel` and `coroutines-core` move from `implementation` to `api` in `commonMain`.

Rationale: `ViewModel` is the superclass of the public store type, so the Kotlin compiler must
resolve it when compiling a subclass. `Flow`, `StateFlow`, and `CoroutineDispatcher` are also part
of the public API. The current runtime-only scope makes subclass compilation depend on the consumer
happening to add those dependencies separately.

Alternative considered: document that consumers must add the dependencies explicitly. Rejected
because the library would expose types it does not declare as compile-time dependencies, and the
failure would appear as a confusing classpath error.

### Test the underlying mechanism, not the frameworks

Common tests will:

- Declare concrete stores through both the five-type-parameter class and the four-type-parameter
  alias, and exercise init, events, effects, and state through the subclasses.
- Request all three stores from one `ViewModelStore` through `ViewModelProvider` and assert that
  they are different instances.

Koin and Navigation 3 are not test dependencies. Their shared dependency is runtime-class identity,
which the `ViewModelProvider` test exercises directly. Koin integration remains a documentation
example.

### Demonstrate both paths in the app

The app demo converts `ExampleStore` to a five-type-parameter subclass and `MinimumStoreExample` to
a four-type-parameter alias subclass. `ExampleFragment` already uses `viewModels<ExampleStore>()`,
so the conversion also demonstrates that a concrete subclass gets its own ViewModel key.

## Risks / Trade-offs

- **Opening a class is a compatibility commitment** -> Keep all behavior methods closed, document
  the inheritance contract, and treat future constructor changes as breaking.
- **Alias vs class type parameter order** -> Document that the four-parameter alias is
  `<State, Effect, Event, Command>` while the five-parameter class is
  `<State, Effect, Event, InternalEvent, Command>`.
- **Alias fixes `InternalEvent` to `Nothing`** -> Document that stores needing an actor and internal
  reducer must extend the five-parameter class.
- **Generic base classes still collide** -> Document that only concrete classes get distinct runtime
  identities.
- **`api` scope changes consumer dependency resolution** -> Update README dependency guidance;
  consumers that exclude the transitive lifecycle dependency can still do so and add their preferred
  version. Verify the published module metadata/POM after the change.
- **No automated Koin/Navigation 3 integration test** -> The `ViewModelProvider` test covers the
  framework-independent mechanism; examples in the README cover usage.

## Migration Plan

The change is additive. Existing code needs no migration.

To adopt named stores, an application declares one concrete subclass per store using either the
four-type-parameter alias or the five-type-parameter class, and registers or retrieves that class
instead of the typealias. Rollback consists of reverting the class to final and restoring the
previous dependency scopes; there is no data migration.

## Open Questions

None.
