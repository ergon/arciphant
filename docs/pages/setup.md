---
icon: lucide/monitor-cog
---

# Arciphant setup

## Prerequisites

### Java or Kotlin plugin

To use Arciphant in your Gradle build, you have to apply either the *Java* or *Kotlin JVM* plugin to all projects in the project hierarchy:

``` kotlin hl_lines="2 6"
plugins {
  kotlin("jvm")
}

subprojects {
  plugins.apply("org.jetbrains.kotlin.jvm")
}
```

This is necessary since Arciphant uses the JVM-specific configurations *api* and *implementation* registered by these plugins.

## Include Arciphant in your build

To integrate Arciphant, add the following to your `settings.gradle.kts`:

``` kotlin title="settings.gradle.kts" hl_lines="2"
plugins {
  id("ch.ergon.arciphant")
}

arciphant {
  // your arciphant config
}
```

Arciphant is a Gradle *settings* plugin. A *settings* plugin is different to normal plugins in that it is applied in the `settings.gradle.kts` file and *NOT* in `build.gradle.kts`.
