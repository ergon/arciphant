plugins {
    `kotlin-dsl`
}

dependencies {
    // required since Kotlin plugin is applied from within the module-plugin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    // resolved from the included build 'arciphant-gradle-plugin' (dependency substitution)
    implementation("ch.ergon.arciphant:arciphant-gradle-plugin")
}
