package ch.ergon.arciphant.core

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ArciphantGradleCompatibilityTest {

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
    fun `source set component layout reuses configuration cache`() {
        createSourceSetComponentLayoutBuild()

        val arguments = arrayOf(
            "validatePackageStructure",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )

        val firstRun = gradleRunner
            .withArguments(*arguments)
            .build()

        val secondRun = gradleRunner
            .withArguments(*arguments)
            .build()

        assertThat(firstRun.task(":test:validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(secondRun.task(":test:validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(secondRun.output).contains("Reusing configuration cache.")
    }

    @Test
    fun `source set component layout is compatible with isolated projects`() {
        createSourceSetComponentLayoutBuild()

        val result = gradleRunner
            .withArguments(
                "validatePackageStructure",
                // Gradle 8.12 still uses the experimental property name. Once the wrapper is updated to
                // Gradle 9.7+, this can be replaced with the --isolated-projects command-line option.
                "-Dorg.gradle.unsafe.isolated-projects=true",
                "--configuration-cache-problems=fail",
            )
            .build()

        assertThat(result.task(":test:validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `project component layout reuses configuration cache`() {
        createProjectComponentLayoutBuild()

        val arguments = arrayOf(
            "validatePackageStructure",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )

        val firstRun = gradleRunner
            .withArguments(*arguments)
            .build()

        val secondRun = gradleRunner
            .withArguments(*arguments)
            .build()

        assertThat(firstRun.task(":test:domain:validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(secondRun.task(":test:domain:validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(secondRun.output).contains("Reusing configuration cache.")
    }

    private fun createSourceSetComponentLayoutBuild() {
        settingsFile.write(
            """
            plugins {
                id("ch.ergon.arciphant")
            }

            // Arciphant's source-set layout expects the JVM plugin to be present on module projects.
            // Applying the core plugin from a state-isolating lifecycle callback keeps the fixture itself
            // compatible with Isolated Projects, so any reported violation originates from Arciphant.
            gradle.lifecycle.beforeProject {
                pluginManager.apply("java-library")
            }

            arciphant {
                sourceSetComponentLayout()
                module("test")
                    .createComponent("domain")
            }
            """
        )
        buildFile.write("")
        // a correctly packaged source file, so the validation task exercises its full path
        sourceFile("test/src/domain/java/test/domain/Domain.java")
    }

    private fun createProjectComponentLayoutBuild() {
        settingsFile.write(
            """
            plugins {
                id("ch.ergon.arciphant")
            }

            gradle.lifecycle.beforeProject {
                pluginManager.apply("java-library")
            }

            arciphant {
                module("test")
                    .createComponent("domain")
            }
            """
        )
        buildFile.write("")
        sourceFile("test/domain/src/main/java/test/domain/Domain.java")
    }

    private fun sourceFile(relativePath: String) {
        projectFolder.resolve(relativePath)
            .write("// content is irrelevant — the validation only checks the folder structure")
    }

    private fun File.write(content: String) {
        parentFile.mkdirs()
        writeText(content.trimIndent())
    }
}
