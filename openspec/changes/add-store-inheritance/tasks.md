# Tasks

## 1. Store inheritance API

- [x] 1.1 Change `GelmStore` to `open class` while keeping the constructor signature and all
  behavior members closed; verify `./gradlew :gelm:compileKotlinJvm` succeeds.
- [x] 1.2 Add `GelmStoreTest_Subclassing.kt` with concrete store subclasses covering init, state,
  events, effects, and actor/internal-reducer flow; verify `./gradlew :gelm:jvmTest` passes.
- [x] 1.3 Extend the test to request all subclasses from one `ViewModelStore` through
  `ViewModelProvider` and assert they are different instances; verify the test passes with concrete
  subclasses.
- [x] 1.4 Add KDoc to `GelmStore` and the four-type-parameter typealias explaining named subclasses
  and the generic-base-class caveat; verify the KDoc renders and matches the implemented
  constructor.
- [x] 1.5 Add a README section with concrete store examples, Koin `viewModelOf` usage, Navigation 3
  `viewModel<Store>()` usage, and the runtime-type distinction; verify every snippet uses the real
  import paths and constructor parameters.
- [x] 1.6 Add an `Unreleased` changelog entry for subclassable stores; verify the entry is present
  and does not claim behavior changes.

## 2. Public API dependencies

- [x] 2.1 Promote `lifecycle-viewmodel` and `coroutines-core` from `implementation` to `api` in
  `commonMain`; verify `./gradlew :gelm:compileKotlinJvm` succeeds.
- [x] 2.2 Verify the published metadata exposes both dependencies on the compile API variant (
  generated POM/module metadata); the lifecycle and coroutines entries must no longer be
  runtime-only.
- [x] 2.3 Update the README dependency section to describe compile-time exposure and keep the
  exclusion/version-override guidance accurate; verify the text matches the generated metadata.

## 3. Verification

- [x] 3.1 Run `./gradlew :gelm:test` (the CI command) and verify it passes.
- [x] 3.2 Run `./gradlew :gelm:compileKotlinIosSimulatorArm64` and verify the native target still
  compiles with the open class and updated dependency scopes.
- [x] 3.3 Run `openspec validate add-store-inheritance --strict` and verify the change passes
  without warnings.

## 4. Inheritance paths, tests, and demo

- [x] 4.1 Correct KDoc in `GelmStore` and the four-type-parameter typealias to document both
  inheritance paths and the `InternalEvent = Nothing` expansion; verify the snippets use the correct
  type-parameter order.
- [x] 4.2 Update the README named-store section to show inheritance through the four-type-parameter
  alias and the five-type-parameter class; verify no text claims the alias cannot be a supertype.
- [x] 4.3 Add a common test store inherited through the four-type-parameter alias, include it in the
  `ViewModelProvider` distinctness test, and verify `./gradlew :gelm:jvmTest` passes.
- [x] 4.4 Convert `ExampleStore` to a five-type-parameter subclass and `MinimumStoreExample` to a
  four-type-parameter alias subclass; verify `./gradlew :app:compileDebugKotlin` succeeds.
- [x] 4.5 Update the changelog with both inheritance paths; verify the entry matches the README.
- [x] 4.6 Run `./gradlew :gelm:test`, `./gradlew :gelm:compileTestKotlinIosSimulatorArm64`, and
  `openspec validate add-store-inheritance --strict`; verify all pass.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived `gelm-store` spec contains the new requirements.
