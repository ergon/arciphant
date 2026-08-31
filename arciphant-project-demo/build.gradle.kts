plugins {
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.spring")
    plugins.apply("org.springframework.boot")
    plugins.apply("io.spring.dependency-management")

    tasks.named("bootJar") { enabled = false }
    tasks.named("jar") { enabled = true }
}

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}
