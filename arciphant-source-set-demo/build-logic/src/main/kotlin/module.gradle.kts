plugins {
    kotlin("jvm")
    // lets Arciphant mark the component test and test fixtures source sets as test sources in IntelliJ IDEA
    idea
}

tasks.named("compileKotlin") {
    dependsOn("validatePackageStructure")
}
