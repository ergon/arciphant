package ch.ergon.arciphant.analyze

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional test for the 'projectDependencies' task.
 *
 * Before running this test with IntelliJ, the project should be built using Gradle.
 * See https://jdriven.com/blog/2021/01/gradlerunner-tests-intellij
 */
class ProjectDependenciesPluginTest {

    @TempDir
    private lateinit var projectFolder: File

    private val settingsFile by lazy { projectFolder.resolve("settings.gradle.kts") }
    private val buildFile by lazy { projectFolder.resolve("build.gradle.kts") }

    private val gradleRunner by lazy {
        GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .forwardOutput()
    }

    @Test
    fun `it should print the wired dependencies of the project layout`() {
        settingsFileWithArciphant(
            """
            module("orders")
                .createComponent("domain")
                .createComponent("application", apiDependencies = setOf("domain"))
                .createComponent("web", dependencies = setOf("application"))
            """
        )
        buildFileWithJvmPlugins()

        val result = gradleRunner.withArguments("-q", "projectDependencies").build()

        assertThat(result.output).contains("Project ':orders:application'")
        assertThat(result.output).contains(" |- [api] :orders:domain")
        assertThat(result.output).contains("Project ':orders:web'")
        assertThat(result.output).contains(" |- [implementation] :orders:application")
    }

    @Test
    fun `it should only print dependencies between projects in the source set layout`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()

            module("producer").createComponent("api")
            module("consumer")
                .createComponent("domain")
                .createComponent("application", dependencies = setOf("domain"))
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("consumer/build.gradle.kts").write(
            """
            dependencies {
                "applicationImplementation"(component(module = "producer", component = "api"))
            }
            """
        )

        val result = gradleRunner.withArguments("-q", "projectDependencies").build()

        // the cross-module dependency is a project dependency and printed …
        assertThat(result.output).contains("Project ':consumer'")
        assertThat(result.output).contains("[applicationImplementation] :producer")
        // … while the component dependencies inside a module are source set wiring and do not appear
        assertThat(result.output).doesNotContain("] :consumer")
    }

    private fun settingsFileWithArciphant(arciphantConfiguration: String) = settingsFile.write(
        """
                plugins {
                    id("ch.ergon.arciphant")
                }

                arciphant {
                    $arciphantConfiguration
                }
                """
    )

    private fun buildFileWithJvmPlugins() = buildFile.write(
        """
        allprojects {
            pluginManager.apply("java-library")
            repositories { mavenCentral() }
        }
        """
    )

    private fun File.write(content: String) {
        parentFile.mkdirs()
        writeText(content.trimIndent())
    }
}
