import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
}

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}
