plugins {
    `filestore-module`
}

dependencies {
    // the certificate domain uses the exam module's public API
    "domainApi"(component(module = "exam", component = "api"))
}
