plugins {
    id("common-module")
}

dependencies {
    // the exam domain uses the course module's public API
    "domainApi"(component(module = "course", component = "api"))
}
