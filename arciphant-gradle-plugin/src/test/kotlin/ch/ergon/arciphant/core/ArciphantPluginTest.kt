package ch.ergon.arciphant.core

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `test that project structure is created according to module configuration`() {
        settingsFileWithArciphant(
            """
            val sampleStencil = stencil {
              addComponent("api")
              addComponent("domain")
            }
            
            module("test") {
              basedOn(sampleStencil)
            }
        """
        )
        val result = gradleRunner
            .withArguments("-q", "projects")
            .build()

        assertThat(result.output).contains("Project ':test:api'")
        assertThat(result.output).contains("Project ':test:domain'")
    }

    @Test
    fun `test that module without components is not allowed`() {
        settingsFileWithArciphant(
            """ 
            arciphant {
                module("test")
            }
        """
        )

        val error = assertThrows<UnexpectedBuildFailure> {
            gradleRunner
                .withArguments("-q", "projects")
                .build()
        }

        assertThat(error.message).contains("Module 'test' does not have any component.")
    }

    private fun settingsFileWithArciphant(arciphantConfiguration: String) = settingsFile.write(
        """
        import ch.ergon.arciphant.core.ArciphantCorePlugin
            
            plugins {
                id("ch.ergon.arciphant.dsl")
            }
            
            arciphant {
                $arciphantConfiguration
            }
            
            apply<ArciphantCorePlugin>()
    """
    )

    private fun File.write(content: String) = writeText(content.trimIndent())

}
