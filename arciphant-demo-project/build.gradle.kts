plugins {
    kotlin("plugin.spring") version "2.2.0" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.spring")
    plugins.apply("org.springframework.boot")
    plugins.apply("io.spring.dependency-management")
}

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}
