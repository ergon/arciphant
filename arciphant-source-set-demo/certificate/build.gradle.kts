plugins {
    `module`
}

dependencies {
    "filestoreImplementation"(libs.minio)
}

arciphantModule {
    // the certificate domain uses the exam module's public API
    component("domain").api("exam", "api")
}
