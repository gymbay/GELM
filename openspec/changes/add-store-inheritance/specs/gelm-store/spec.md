# Spec Delta

## Purpose

Defines how application code constructs GELM stores, including named store subclasses that remain
behaviorally identical to the base store while having a distinct runtime identity for DI and
navigation scoping.

## ADDED Requirements

### Requirement: Named store subclasses

The library SHALL allow application code to declare a concrete named class that inherits from
`GelmStore` through either the five-type-parameter class or the four-type-parameter alias. The
subclass SHALL retain the store's normal initial-state processing, event handling, effect emission,
and command handling without additional application code.

#### Scenario: Concrete subclass processes events

- **WHEN** an application declares a concrete `GelmStore` subclass with an initial state and an
  external reducer
- **THEN** creating the subclass processes the init phase and emits the initial state
- **AND** sending an event through the subclass updates the state and emits effects using the
  supplied reducer

#### Scenario: Concrete subclass supports actor and internal reducer

- **WHEN** an application declares a concrete `GelmStore` subclass with an actor and an internal
  reducer
- **THEN** commands produced by the external reducer are executed by the actor
- **AND** internal events from the actor are processed by the internal reducer and update the state

#### Scenario: Subclass through the four-parameter alias

- **WHEN** an application declares a concrete subclass whose supertype is the four-type-parameter
  alias
- **THEN** the code compiles and the alias expands to the five-type-parameter class with
  `InternalEvent = Nothing`
- **AND** the subclass processes init, events, and effects like a store without internal events

### Requirement: Distinct runtime identity

Each concrete `GelmStore` subclass SHALL have its own runtime class identity. Two different concrete
subclasses SHALL NOT share a default ViewModel key when requested from the same `ViewModelStore`, so
frameworks that distinguish ViewModels or bindings by runtime class can manage them independently.

#### Scenario: Different subclasses get different ViewModel instances

- **WHEN** two different concrete `GelmStore` subclasses are requested from the same
  `ViewModelStore` through `ViewModelProvider` without explicit keys
- **THEN** two distinct store instances are returned

#### Scenario: DI definitions are distinguishable by class

- **WHEN** a dependency-injection container keys definitions by runtime class
- **THEN** two different concrete `GelmStore` subclasses are resolved as separate definitions

### Requirement: No behavior overrides through inheritance

Store subclasses SHALL be able to add a distinct type identity only. Event processing, state
emission, effect emission, command execution, and reducer orchestration SHALL NOT be overridable by
store subclasses.

#### Scenario: Subclass cannot replace store behavior

- **WHEN** an application declares a concrete `GelmStore` subclass
- **THEN** it cannot override event sending, state emission, effect emission, or reducer processing
  behavior

### Requirement: Backward-compatible direct usage

`GelmStore` SHALL remain directly instantiable with its existing constructor, and the
four-type-parameter typealias SHALL remain available and continue to resolve to the base store
class. Existing source code that uses either form SHALL continue to compile, and runtime behavior of
existing stores SHALL remain unchanged.

#### Scenario: Direct construction still works

- **WHEN** existing code constructs `GelmStore` directly with an initial state and reducers
- **THEN** it compiles and behaves as before

#### Scenario: Four-parameter typealias still works

- **WHEN** existing code uses the four-type-parameter typealias to type or construct a store
- **THEN** it compiles and refers to the base store class

### Requirement: Compile-time API dependencies

The published library metadata SHALL expose the lifecycle ViewModel and coroutines types used by the
public API as compile-time dependencies, so a consumer can compile a concrete `GelmStore` subclass
without adding separate compile dependencies for those types.

#### Scenario: Consumer compiles a subclass

- **WHEN** a consumer compiles a concrete `GelmStore` subclass with GELM as a dependency
- **THEN** the compiler resolves `ViewModel`, `Flow`, `StateFlow`, and `CoroutineDispatcher` from
  GELM's exposed API dependencies
