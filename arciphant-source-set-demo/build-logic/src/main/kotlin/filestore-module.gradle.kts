import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("common-module")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "filestoreImplementation"(libs.findLibrary("minio").get().get())
}
