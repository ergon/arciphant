---
icon: lucide/monitor-cog
---

# Arciphant setup

## Prerequisites

### Java or Kotlin plugin

In order to use Arciphant in your Gradle build you have to apply either the *Java* or *Kotlin JVM* plugin to all projects in the project hierarchy
(the reason is that Arciphant builds upon the JVM-specific configurations *api* and *implementation* registered by these plugins).

This can typically be done in the root project, e.g. like the following:

``` kotlin hl_lines="2 6"
plugins {
  kotlin("jvm")
}

subprojects {
  plugins.apply("org.jetbrains.kotlin.jvm")
}
```


Of course, you can also apply the *Java* or *Kotlin JVM* plugin inside a convention plugin and then apply this convention plugin to all projects.
It is also possible to register such a convention plugin in Arciphant. See chapter *Using Plugins*.

## Include Arciphant in your build

Arciphant is a Gradle *settings* plugin. A settings plugin is different to normal plugins in that it is applied in the `settings.gradle.kts` file and *NOT* in `build.gradle.kts`.

For the configuration of Arciphant, the plugin provides a simple DSL. The top-level element of the DSL is `arciphant {}`. So put the following to your `settings.gradle.kts`

``` kotlin title="settings.gradle.kts" hl_lines="2 5-7"
plugins {
  id("ch.ergon.arciphant")
}

arciphant {
  // your arciphant config
}
```