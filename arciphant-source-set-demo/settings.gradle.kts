pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("./build-logic")
    includeBuild("../arciphant-gradle-plugin")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("ch.ergon.arciphant")
    id("module") apply false // solely used to ensure plugin resolution mechanism for prebuilt plugins in 'build-logic' is triggered.
}

arciphant {
    sourceSetComponentLayout()

    val commonModuleTemplate = template()
        .createComponent(name = "api") // the module's public contract, usable from other modules
        .createComponent(name = "domain", apiDependencies = setOf("api"))
        .createComponent(name = "db", dependencies = setOf("domain"))
        .createComponent(name = "webApi")
        .createComponent(name = "web", dependencies = setOf("webApi", "domain"))

    val moduleWithFilestoreTemplate = template()
        .extends(commonModuleTemplate)
        .createComponent(name = "filestore", dependencies = setOf("domain"))

    library(name = "shared", template = moduleWithFilestoreTemplate)

    module(name = "course", template = commonModuleTemplate)
    module(name = "exam", template = commonModuleTemplate)
    module(name = "certificate", template = moduleWithFilestoreTemplate)
        .createComponent(name = "certificateAuthorityAdapter", dependencies = setOf("domain"))
    module(name = "accounting", template = moduleWithFilestoreTemplate)
        .createComponent(name = "paymentProviderAdapter", dependencies = setOf("domain"))
        .extendComponent(name = "web", dependencies = setOf("paymentProviderAdapter"))

    bundle(name = "online-learning-platform")

    packageStructureValidation {
        val basePackage = "ch.ergon.arciphant.example"
        basePackageName(basePackage)
        mapProjectPathsToAbsolutePackages(":online-learning-platform" to basePackage)
    }
}
