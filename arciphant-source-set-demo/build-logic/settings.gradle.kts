// makes the arciphant plugin available on the compile classpath of the precompiled script plugins
includeBuild("../../arciphant-gradle-plugin")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
