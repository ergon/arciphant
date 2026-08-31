plugins {
    `kotlin-dsl`
}

dependencies {
    // required since Kotlin plugin is applied from within the component-plugin
    implementation(libs.kotlin.gradle.plugin)
}
