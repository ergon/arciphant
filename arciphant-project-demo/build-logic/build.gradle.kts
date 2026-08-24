plugins {
    `kotlin-dsl`
}

dependencies {
    // required since Kotlin plugin is applied from within the component-plugin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
}
