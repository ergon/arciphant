package ch.ergon.arciphant.core

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Properties

/**
 * Tests the project-side application of the arciphant plugin, in particular its use inside
 * precompiled script plugins of an included 'build-logic' build.
 *
 * Before running this test with IntelliJ, the project should be built using Gradle.
 * See https://jdriven.com/blog/2021/01/gradlerunner-tests-intellij
 */
class ArciphantProjectPluginTest {

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
    fun `test that module DSL can be used in a precompiled script plugin`() {
        settingsFile.write(
            """
            pluginManagement {
                includeBuild("build-logic")
            }

            plugins {
                id("ch.ergon.arciphant")
            }

            arciphant {
                sourceSetComponentLayout()

                module("producer").createComponent("api")
                module("consumer").createComponent("application")
            }
            """
        )
        buildLogicWithConventionPlugin(
            """
            plugins {
                id("ch.ergon.arciphant")
                `java-library`
            }

            arciphantModule {
                component("application").implementation(module = "producer", component = "api")
            }
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("consumer/build.gradle.kts").write(
            """
            plugins {
                id("arciphant-convention")
            }
            """
        )
        projectFolder.resolve("producer/src/api/java/example/producer/Api.java").write(
            """
            package example.producer;

            public class Api {}
            """
        )
        projectFolder.resolve("consumer/src/application/java/example/consumer/Application.java").write(
            """
            package example.consumer;

            import example.producer.Api;

            public class Application extends Api {}
            """
        )

        val result = gradleRunner.withArguments(":consumer:compileApplicationJava").build()

        assertThat(result.task(":producer:compileApiJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":consumer:compileApplicationJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).doesNotContain(MISSING_SETTINGS_PLUGIN_WARNING)
    }

    @Test
    fun `test that component DSL can be used in a precompiled script plugin`() {
        settingsFile.write(
            """
            pluginManagement {
                includeBuild("build-logic")
            }

            plugins {
                id("ch.ergon.arciphant")
            }

            arciphant {
                module("producer").createComponent("api")
                module("consumer").createComponent("application")
            }
            """
        )
        buildLogicWithConventionPlugin(
            """
            plugins {
                id("ch.ergon.arciphant")
                `java-library`
            }

            arciphantComponent {
                implementation(module = "producer", component = "api")
            }
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("consumer/application/build.gradle.kts").write(
            """
            plugins {
                id("arciphant-convention")
            }
            """
        )
        projectFolder.resolve("producer/api/src/main/java/example/producer/Api.java").write(
            """
            package example.producer;

            public class Api {}
            """
        )
        projectFolder.resolve("consumer/application/src/main/java/example/consumer/Application.java").write(
            """
            package example.consumer;

            import example.producer.Api;

            public class Application extends Api {}
            """
        )

        val result = gradleRunner.withArguments(":consumer:application:compileJava").build()

        assertThat(result.task(":producer:api:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":consumer:application:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).doesNotContain(MISSING_SETTINGS_PLUGIN_WARNING)
    }

    @Test
    fun `test that a warning is logged when the plugin is applied without the settings plugin`() {
        settingsFile.write(
            """
            rootProject.name = "no-arciphant"
            """
        )
        buildFile.write(
            """
            plugins {
                id("ch.ergon.arciphant")
            }
            """
        )

        val result = gradleRunner.withArguments("help").build()

        assertThat(result.output).contains(MISSING_SETTINGS_PLUGIN_WARNING)
    }

    /**
     * Creates an included 'build-logic' build containing the precompiled script plugin 'arciphant-convention'.
     * The arciphant plugin is put on the compile classpath of 'build-logic' using the same classpath files that
     * [GradleRunner.withPluginClasspath] injects into the main build.
     */
    private fun buildLogicWithConventionPlugin(script: String) {
        projectFolder.resolve("build-logic/settings.gradle.kts").write(
            """
            rootProject.name = "build-logic"
            """
        )
        projectFolder.resolve("build-logic/build.gradle.kts").write(
            """
            plugins {
                `kotlin-dsl`
            }

            repositories {
                mavenCentral()
                gradlePluginPortal()
            }

            dependencies {
                implementation(files(${pluginClasspath()}))
            }
            """
        )
        projectFolder.resolve("build-logic/src/main/kotlin/arciphant-convention.gradle.kts").write(script)
    }

    private fun pluginClasspath(): String {
        val metadata = checkNotNull(javaClass.classLoader.getResourceAsStream(PLUGIN_UNDER_TEST_METADATA)) {
            "'$PLUGIN_UNDER_TEST_METADATA' not found on test classpath. Build the project with Gradle first."
        }
        val properties = metadata.use { Properties().apply { load(it) } }
        return properties.getProperty("implementation-classpath")
            .split(File.pathSeparator)
            .joinToString(", ") { "\"${File(it).invariantSeparatorsPath}\"" }
    }

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

    companion object {
        private const val MISSING_SETTINGS_PLUGIN_WARNING = "but the Arciphant settings plugin"
        private const val PLUGIN_UNDER_TEST_METADATA = "plugin-under-test-metadata.properties"
    }
}
