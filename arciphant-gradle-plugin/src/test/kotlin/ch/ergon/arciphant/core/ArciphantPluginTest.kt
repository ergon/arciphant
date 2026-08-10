package ch.ergon.arciphant.core

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Before running this test with IntelliJ, the project should be built using Gradle.
 * See https://jdriven.com/blog/2021/01/gradlerunner-tests-intellij
 */
class ArciphantPluginTest {

    @TempDir
    private lateinit var projectFolder: File

    private val settingsFile by lazy { projectFolder.resolve("settings.gradle.kts") }

    private val gradleRunner by lazy {
        GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .forwardOutput()
    }

    @Test
    fun `test that library is created according to configuration`() {
        settingsFileWithArciphant(
            """
            val sampleTemplate = template()
                .createComponent("api")
                .createComponent("domain")
            
            library("test", template = sampleTemplate)
            """
        )
        val result = gradleRunner
            .withArguments("-q", "projects")
            .build()

        assertThat(result.output).contains("Project ':test:api'")
        assertThat(result.output).contains("Project ':test:domain'")
    }

    @Test
    fun `test that module is created according to configuration`() {
        settingsFileWithArciphant(
            """
            val sampleTemplate = template()
                .createComponent("api")
                .createComponent("domain")
            
            module("test", template = sampleTemplate)
            """
        )
        val result = gradleRunner
            .withArguments("-q", "projects")
            .build()

        assertThat(result.output).contains("Project ':test:api'")
        assertThat(result.output).contains("Project ':test:domain'")
    }

    @Test
    fun `test that package structure validation is scoped to the selected project`() {
        settingsFileWithArciphant(
            """
            packageStructureValidation {
                basePackageName("com.example")
            }

            val sampleTemplate = template()
                .createComponent("api")
                .createComponent("domain")

            module("test", template = sampleTemplate)
            """
        )
        projectFolder.resolve("test/api/src/main/kotlin/com/example/test/api/Valid.kt").write(
            """
            package com.example.test.api
            """
        )
        val invalidSourceFile = projectFolder.resolve("test/domain/src/main/kotlin/wrong/Invalid.kt")
        invalidSourceFile.write(
            """
            package wrong
            """
        )

        val validResult = gradleRunner
            .withArguments(":test:api:validatePackageStructure")
            .build()
        assertThat(validResult.task(":test:api:validatePackageStructure")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        val invalidResult = gradleRunner
            .withArguments("validatePackageStructure")
            .buildAndFail()
        assertThat(invalidResult.task(":test:domain:validatePackageStructure")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
        assertThat(invalidResult.output)
            .contains("Source file '${invalidSourceFile.path}' has invalid package name.")
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

    private fun File.write(content: String) {
        parentFile.mkdirs()
        writeText(content.trimIndent())
    }
}
