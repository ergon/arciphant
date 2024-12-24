package ch.cbossi.gradle.playground.build

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File


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

        assertThat(result.output).contains("Project ':test:api'")
        assertThat(result.output).contains("Project ':test:domain'")
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

        assertThat(error.message).contains("Module 'test' has no components.")
    }

    private fun buildSettingsFileWithModulithPlugin(modulithConfiguration: String) = buildSettingsFile(
        """
        import ch.cbossi.gradle.playground.build.ModulithSettingsPlugin
            
            plugins {
                id("ch.cbossi.gradle.playground.build.modulith-configuration-settings-plugin")
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
