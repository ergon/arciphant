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
    id("spring-component") apply false // solely used to ensure plugin resolution mechanism for prebuilt plugins in 'build-logic' is triggered.
}

arciphant {
    val commonModuleTemplate = template()
        .createComponent(name = "api", plugin = "spring-component")
        .createComponent(name = "domain", plugin = "spring-component", dependsOnApi = setOf("api"))
        .createComponent(name = "db", plugin = "jooq-component", dependsOn = setOf("domain"))
        .createComponent(name = "web-api", plugin = "spring-web-component")
        .createComponent(name = "web", plugin = "spring-web-component", dependsOn = setOf("web-api", "domain"))

    val moduleWithFilestoreTemplate = template(basedOn = commonModuleTemplate)
        .createComponent(name = "filestore", plugin = "minio-component", dependsOn = setOf("domain"))

    library(name = "shared", template = moduleWithFilestoreTemplate)

    module(name = "course", template = commonModuleTemplate)
    module(name = "exam", template = commonModuleTemplate)
    module(name = "certificate", template = moduleWithFilestoreTemplate)
        .createComponent(name = "certificate-authority-adapter", plugin = "spring-component", dependsOn = setOf("domain"))
    module(name = "accounting", template = moduleWithFilestoreTemplate)
        .createComponent(name = "payment-provider-adapter", plugin = "spring-component", dependsOn = setOf("domain"))
        .extendComponent(name = "web", dependsOn = setOf("payment-provider-adapter"))

    bundle(name = "online-learning-platform", plugin = "spring-boot-bundle-module")
}
