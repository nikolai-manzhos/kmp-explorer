# Kmp Explorer
Gradle plugin that helps visualize relations between KMP's source sets.

# Getting Started

Deploy the latest version to maven-local:
```bash
make publish
```

Make sure that `mavenLocal()` included as project's repository: 

```kotlin
pluginManagement {
    // ...
    repositories {
        // ...
        mavenLocal()
    }
}
```

Apply KmpExplorer to your project's root:
```kotlin
plugins {
    id("com.kmp.explorer") version "0.2"
}

// Optional, configure output format:
kmpExplorer {
    format = Format.PNG
}
```
# Usage
The plugin will generate a set of tasks for each KMP project:
* exploreKmpGraph
* exploreMainKmpGraph
* exploreTestKmpGraph

```bash
./gradlew sample:composeApp:exploreKmpGraph
```
 You will find KMP graph representation under `sample/composeApp/build`.

# Development

You can build the plugin by running `make assemble` and execute all tests using `make test`.
