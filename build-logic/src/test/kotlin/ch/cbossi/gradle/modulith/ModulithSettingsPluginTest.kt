package ch.cbossi.gradle.modulith

import org.assertj.core.api.Assertions
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
class ModulithSettingsPluginTest {

    @TempDir
    lateinit var projectFolder: File

    private val gradleRunner by lazy {
        GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .forwardOutput()
    }

    @Test
    fun `test that project structure is created according to module configuration`() {
        buildSettingsFileWithModulithPlugin(
            """
            allModules {
                addComponent("api")
                addComponent("domain")
            }
            
            module("test")
        """
        )
        val result = gradleRunner
            .withArguments("-q", "projects")
            .build()

        Assertions.assertThat(result.output).contains("Project ':test:api'")
        Assertions.assertThat(result.output).contains("Project ':test:domain'")
    }

    @Test
    fun `test that module without components is not allowed`() {
        buildSettingsFileWithModulithPlugin(
            """
            modulith {
                module("test")
            }
        """
        )

        val error = assertThrows<UnexpectedBuildFailure> {
            gradleRunner
                .withArguments("-q", "projects")
                .build()
        }

        Assertions.assertThat(error.message).contains("Module 'test' has no components.")
    }

    private fun buildSettingsFileWithModulithPlugin(modulithConfiguration: String) = buildSettingsFile(
        """
        import ch.cbossi.gradle.modulith.ModulithSettingsPlugin
            
            plugins {
                id("ch.cbossi.gradle.modulith.modulith-configuration-settings-plugin")
            }
            
            modulith {
                $modulithConfiguration
            }
            
            apply<ModulithSettingsPlugin>()
    """
    )

    private fun buildSettingsFile(content: String) = buildFile("settings.gradle.kts", content)

    private fun buildFile(name: String, content: String) {
        val buildFile = File(projectFolder, name)
        buildFile.writeText(content.trimIndent())
    }

}