plugins {
    id("filestore-module")
}

dependencies {
    // the certificate domain uses the course module's public API
    "domainApi"(component(module = "course", component = "api"))
}
