plugins {
    kotlin("jvm")
}

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}
