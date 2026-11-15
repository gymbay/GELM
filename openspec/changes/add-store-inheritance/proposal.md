# Proposal

## Why

Koin and Navigation 3 distinguish ViewModels by their runtime class. A `typealias` does not create a
new runtime type, so every store declared as `typealias LoginStore = GelmStore<...>` is the same
`GelmStore` class at runtime. As a result, Koin definitions conflict and `ViewModelProvider` can
reuse the wrong store within one `ViewModelStore`.

## What Changes

- Make `GelmStore` an `open` class so application code can declare a concrete, named store class per
  screen or feature.
- Keep the existing constructor, `sendEvent`, `state`, `effect`, and reducer behavior unchanged; no
  store methods become overridable.
- Keep `io.github.gymbay.gelm.utils.GelmStore` as a four-type-parameter typealias, keep direct
  `GelmStore(...)` instantiation working, and allow the alias to be used as a supertype.
- Document both inheritance paths: the four-type-parameter alias (which fixes `InternalEvent` to
  `Nothing`) and the five-type-parameter class (required for actor/internal-reducer stores).
  Concrete subclasses, not generic base classes, are required for distinct runtime types.
- Promote `lifecycle-viewmodel` and `coroutines-core` to `api` dependencies in `commonMain`, because
  `ViewModel`, `Flow`, `StateFlow`, and `CoroutineDispatcher` are part of the library's public
  contract and consumers must be able to compile subclasses.
- Add tests covering subclass behavior through both inheritance paths and distinct
  `ViewModelProvider` keys, document both paths in the README, and convert the app demo stores to
  concrete subclasses.

## Capabilities

### New Capabilities

- `gelm-store`: Store construction and extensibility behavior, including named subclasses, runtime
  type identity for DI/navigation, and backward-compatible direct usage.

### Modified Capabilities

None.

## Impact

- `gelm/src/commonMain/kotlin/io/github/gymbay/gelm/GelmStore.kt`
- `gelm/src/commonMain/kotlin/io/github/gymbay/gelm/utils/Typealiases.kt`
- `gelm/build.gradle.kts` (dependency scopes)
- `gelm/src/commonTest/kotlin/io/github/gymbay/gelm/` (new subclassing test)
- `app/src/main/java/io/github/gymbay/gelm/app/example/store/ExampleStore.kt` (five-type-parameter
  subclass demo)
- `app/src/main/java/io/github/gymbay/gelm/app/example/store/MinimumStoreExample.kt` (
  four-type-parameter alias subclass demo)
- `README.md`, `CHANGELOG.md`
- No runtime behavior change for existing stores. The published Gradle metadata/POM will expose
  lifecycle and coroutines at compile scope, which may affect consumers that relied on them being
  runtime-only.
