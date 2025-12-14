---
icon: lucide/blocks
---

# Registering Gradle plugins in Arciphant components

## Component plugins

The real power of Arciphant comes into play when you use it in combination with convention plugins that configure your component's characteristic and external dependencies.

Convention plugins are pre-compiled script plugins that contain build logic.
See the [official Gradle documentation](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html) for detailed information.
They can bei either located in the `buildSrc` folder or in a separate gradle project that is included with `includeBuild` in `pluginManagement`.

Assuming that each of the components in your modules has some specific gradle setup such as external dependencies and the configuration around them.
For example in the web component there might be the dependency to a web-framework and in the db-layer a library that manages database access.

So assume you have the following convention plugins in your `buildSrc` folder specifying the dependencies/configurations for the respective component types:

* `spring-web-component.gradle.kts`
* `jooq-component.gradle.kts`

You can register these plugins for the components in Arciphant and they will be applied:

``` kotlin title="settings.gradle.kts"
template()
    .createComponent(name = "web", plugin = "spring-web-component")
    .createComponent(name = "db", plugin = "jooq-component")
```

## Use `includeBuild` instead of `buildSrc`

It is good practice to use a dedicated project for convention plugins instead of using `buildSrc`-folder and include it with `includeBuild`:
``` kotlin title="settings.gradle.kts" hl_lines="3"
pluginManagement {
  [..]
  includeBuild("./build-logic")
}
```

Unfortunately, these plugins won't work together with arciphant out-of-the-box. The problem is that for some reason
the `pluginManagement` block is only treated by gradle when plugins are applied in the `plugins` block.
Since arciphant applies plugins programmatically (using `pluginManager.apply("plugin-id")`), the plugin will not be found.
To overcome this issue you can use the following simple hack: just add one of the convention plugins to the plugins block of your `settings.gradle.kts` file with `apply false`:
``` kotlin title="settings.gradle.kts" hl_lines="3"
plugins {
  [..]
  id("my-plugin") apply false // (1)!
}
```

1.  Overcome plugin resolution limitation of Gradle by adding plugin without applying it.

This seems to trigger the required plugin resolution strategy and arciphant is able to apply all plugins from the included build
(even if only one plugin is referenced in `settings.gradle.kts`).