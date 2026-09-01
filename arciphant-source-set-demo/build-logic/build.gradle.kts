plugins {
    `kotlin-dsl`
}

dependencies {
    // required since Kotlin plugin is applied from within the module-plugin
    implementation(libs.kotlin.gradle.plugin)
}
