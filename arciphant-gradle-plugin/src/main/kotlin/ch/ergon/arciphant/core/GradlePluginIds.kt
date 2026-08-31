package ch.ergon.arciphant.core

object GradlePluginIds {

    /**
     * The 'java' plugin is also applied transitively by the [KOTLIN_JVM] plugin.
     */
    const val JAVA = "java"
    const val JAVA_LIBRARY = "java-library"
    const val KOTLIN_JVM = "org.jetbrains.kotlin.jvm"

    const val JAVA_TEST_FIXTURES = "java-test-fixtures"

    const val IDEA = "idea"
}