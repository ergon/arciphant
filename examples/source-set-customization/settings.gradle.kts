pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../../arciphant-gradle-plugin")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("ch.ergon.arciphant")
}

arciphant {
    sourceSetComponentLayout()
    // the source directories use a custom layout (see 'inventory/build.gradle.kts'),
    // so the default 'src/<sourceSet>' folders must not be created
    disableFolderCreation()

    module(name = "inventory")
        .createComponent(name = "api")
        .createComponent(name = "domain", apiDependencies = setOf("api"))
        .createComponent(name = "web", dependencies = setOf("domain"))
}
