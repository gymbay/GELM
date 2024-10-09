# General

GELM is Android library for the popular presentation approach The ELM Architecture (TEA).

The library standardizes the work with sending and processing synchronous and asynchronous events.
All inputs and outputs to library are strictly defined, which makes it easier to develop and test.

![Gelm schema](/gelm_schema.jpg)

# How to implement (Gradle)

Library publicated in Maven Central repository, so you need first define Maven Central repository in
your settings.gradle.kts file.

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

When you defined Maven Central then you need define library in your application module in
build.gradle.kts file.

```kotlin
dependencies {
    implementation("io.github.gymbay:gelm:1.0.0")
}
```

# Quick start

This guide helps you to understand how library works.

## Data types

Library works with 5 generic types, some types may be optional.
Optional types can be defined as Kotlin Nothing type.

All types applied in [GelmStore](gelm/src/main/java/io/github/gymbay/gelm/GelmStore.kt).

- **State** (required) - UI representation as some Kotlin type. In general is Data class;
- **Effect** (optional) - one shot event. As example, showing alert in Android view, start timer,
  trigger navigation.
  In general is Enum class;
- **Event** (optional) - some external event
  for [GelmStore](gelm/src/main/java/io/github/gymbay/gelm/GelmStore.kt).
  As example, user initiated event from UI, event from
  another [GelmStore](gelm/src/main/java/io/github/gymbay/gelm/GelmStore.kt). In general is Sealed
  or Enum class;
- **InternalEvent** (optional) - some event from
  internal [GelmActor](gelm/src/main/java/io/github/gymbay/gelm/GelmActor.kt). As example, response
  from server or database;
- **Command** (optional) - internal async command
  for [GelmActor](gelm/src/main/java/io/github/gymbay/gelm/GelmActor.kt). May be produced from
  external and internal reducer. In example, command for load data from serve or save to database.

## Main components

Gelm works with 4 main and 2 additional components. In main components only 2 required and others
optional.

Minimum work configuration
required [GelmStore](gelm/src/main/java/io/github/gymbay/gelm/GelmStore.kt)
and [GelmExternalReducer](gelm/src/main/java/io/github/gymbay/gelm/reducers/GelmExternalReducer.kt).

### GelmStore

Central entity for GELM architecture. Holder for architecture components. Responsible for coordinate
flow of external and internal events.

```kotlin
import io.github.gymbay.gelm.utils.GelmStore

// Minimum configuration
GelmStore(
  initialState = ExampleState(),
  externalReducer = ExampleExternalReducer()
)
```

### GelmExternalReducer

### GelmActor

### GelmInternalReducer

## Additional components

### GelmSavedStateHandler

### GelmLogger

## Sending events

## Handle events

## Observing State changes

## Observing Effects

## Async events

## Store observing

# How to test

## Test GelmExternalReducer

## Test GelmActor

## Test GelmInternalReducer
