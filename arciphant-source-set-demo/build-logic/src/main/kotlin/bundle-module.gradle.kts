import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named("jar") { enabled = false }
tasks.named("bootJar") { enabled = true }

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}
