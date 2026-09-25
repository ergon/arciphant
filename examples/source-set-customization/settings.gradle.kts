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

    module(name = "inventory")
        .createComponent(name = "api")
        .createComponent(name = "domain", apiDependencies = setOf("api"))
        .createComponent(name = "web", dependencies = setOf("domain"))

    // the package structure validation follows the customized source directories
    packageStructureValidation {
        basePackageName("ch.ergon.arciphant.example")
    }
}
