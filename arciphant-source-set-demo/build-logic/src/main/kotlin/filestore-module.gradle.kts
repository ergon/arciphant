plugins {
    id("common-module")
}

dependencies {
    "filestoreImplementation"(libs.findLibrary("minio").get())
}
