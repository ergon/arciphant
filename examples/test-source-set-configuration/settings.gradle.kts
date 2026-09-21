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

    // tests are enabled for every component, test fixtures only where a component opts in
    withTestSourceSet(true)
    withTestFixturesSourceSet(false)

    // rename the source sets (and their 'src/<name>' folders):
    // 'domainSpec' and 'domainFixtures' instead of the defaults 'domainTest' and 'domainTestFixtures'
    testSourceSetName { componentName -> "${componentName}Spec" }
    testFixturesSourceSetName { componentName -> "${componentName}Fixtures" }

    module(name = "orders")
        .createComponent(name = "api")
        // the domain component opts back in to test fixtures, overriding the global flag
        .createComponent(name = "domain", apiDependencies = setOf("api"), withTestFixturesSourceSet = true)
}
