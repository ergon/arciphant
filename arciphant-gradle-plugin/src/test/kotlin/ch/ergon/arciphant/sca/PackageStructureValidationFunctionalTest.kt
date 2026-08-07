package ch.ergon.arciphant.sca

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
class PackageStructureValidationFunctionalTest {

    @TempDir
    private lateinit var projectFolder: File

    private val gradleRunner by lazy {
        GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .forwardOutput()
    }

    @Test
    fun `it should accept source files in the package configured with a project level package name`() {
        settingsFile()
        componentBuildFile("""packageName = "ppa"""")
        sourceFile("accounting/payment-provider-adapter/src/main/kotlin/com/acme/accounting/ppa/PaymentProvider.kt")

        val result = gradleRunner.withArguments("validatePackageStructure").build()

        assertThat(result.task(":validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `it should report source files outside the package configured with a project level package name`() {
        settingsFile()
        componentBuildFile("""packageName = "ppa"""")
        sourceFile("accounting/payment-provider-adapter/src/main/kotlin/com/acme/accounting/paymentprovideradapter/PaymentProvider.kt")

        val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

        assertThat(result.output).contains("has invalid package name")
    }

    @Test
    fun `it should accept source files in the package configured with a project level absolute package name`() {
        settingsFile()
        componentBuildFile("""absolutePackageName = "com.other"""")
        sourceFile("accounting/payment-provider-adapter/src/main/kotlin/com/other/PaymentProvider.kt")

        val result = gradleRunner.withArguments("validatePackageStructure").build()

        assertThat(result.task(":validatePackageStructure")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `it should fail if both package name and absolute package name are configured`() {
        settingsFile()
        componentBuildFile(
            """
            packageName = "ppa"
            absolutePackageName = "com.other"
            """.trimIndent()
        )

        val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

        assertThat(result.output)
            .contains("Arciphant configuration error")
            .contains("must not configure both 'packageName' and 'absolutePackageName'")
    }

    private fun settingsFile() = projectFolder.resolve("settings.gradle.kts").write(
        """
        plugins {
            id("ch.ergon.arciphant")
        }

        arciphant {
            val sampleTemplate = template()
                .createComponent("payment-provider-adapter")

            library("accounting", template = sampleTemplate)

            packageStructureValidation {
                basePackageName("com.acme")
            }
        }
        """
    )

    private fun componentBuildFile(arciphantConfiguration: String) =
        projectFolder.resolve("accounting/payment-provider-adapter/build.gradle.kts")
            .also { it.parentFile.mkdirs() }
            .write(
                """
                plugins {
                    id("ch.ergon.arciphant")
                }

                arciphant {
                    $arciphantConfiguration
                }
                """
            )

    private fun sourceFile(relativePath: String) {
        projectFolder.resolve(relativePath).apply {
            parentFile.mkdirs()
            writeText("// test source file")
        }
    }

    private fun File.write(content: String) = writeText(content.trimIndent())

}
