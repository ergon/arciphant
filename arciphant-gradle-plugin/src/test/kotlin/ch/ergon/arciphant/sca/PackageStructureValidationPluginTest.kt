package ch.ergon.arciphant.sca

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional test for the 'validatePackageStructure' task in both component layouts.
 *
 * Before running this test with IntelliJ, the project should be built using Gradle.
 * See https://jdriven.com/blog/2021/01/gradlerunner-tests-intellij
 */
class PackageStructureValidationPluginTest {

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

    @Nested
    inner class ProjectLayout {

        @Test
        fun `it should accept a valid package structure`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders")
                    .createComponent("domain")
                    .createComponent("web-api")
                """
            )
            sourceFile("orders/domain/src/main/kotlin/com/example/orders/domain/Order.kt")
            sourceFile("orders/domain/src/main/kotlin/com/example/orders/domain/sub/Nested.kt")
            sourceFile("orders/domain/src/test/kotlin/com/example/orders/domain/OrderTest.kt")
            sourceFile("orders/web-api/src/main/kotlin/com/example/orders/webapi/OrderController.kt")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
            assertThat(result.task(":orders:web-api:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should reject wrong module and component packages`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders")
                    .createComponent("domain")
                    .createComponent("web")
                """
            )
            val wrongModule = sourceFile("orders/domain/src/main/kotlin/com/example/other/domain/Order.kt")
            val missingComponent = sourceFile("orders/web/src/main/kotlin/com/example/orders/OrderController.kt")

            val result = gradleRunner.withArguments("validatePackageStructure", "--continue").buildAndFail()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.FAILED)
            assertThat(result.task(":orders:web:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.FAILED)
            assertThat(result.output).contains("Source file '${wrongModule.path}' has invalid package name.")
            assertThat(result.output).contains("Source file '${missingComponent.path}' has invalid package name.")
        }
    }

    @Nested
    inner class SourceSetLayout {

        @Test
        fun `it should accept a valid package structure`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders")
                    .createComponent("domain")
                    .createComponent("webApi", dependencies = setOf("domain"))
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("orders/src/domain/java/com/example/orders/domain/Order.java")
            sourceFile("orders/src/domain/java/com/example/orders/domain/sub/Nested.java")
            sourceFile("orders/src/domainTest/java/com/example/orders/domain/OrderTest.java")
            sourceFile("orders/src/domainTestFixtures/java/com/example/orders/domain/OrderFixtures.java")
            sourceFile("orders/src/webApi/java/com/example/orders/webapi/OrderController.java")
            sourceFile("orders/src/main/java/com/example/orders/ModuleWide.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should reject wrong module and component packages`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders")
                    .createComponent("domain")
                    .createComponent("webApi", dependencies = setOf("domain"))
                """
            )
            buildFileWithJvmPlugins()
            val missingComponent = sourceFile("orders/src/domain/java/com/example/orders/Order.java")
            val wrongComponent = sourceFile("orders/src/domain/java/com/example/orders/webapi/Order.java")
            val wrongModule = sourceFile("orders/src/domain/java/com/example/other/domain/Order.java")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.task(":orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.FAILED)
            assertThat(result.output).contains("Source file '${missingComponent.path}' has invalid package name.")
            assertThat(result.output).contains("Source file '${wrongComponent.path}' has invalid package name.")
            assertThat(result.output).contains("Source file '${wrongModule.path}' has invalid package name.")
        }

        @Test
        fun `it should validate custom test source set names`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()
                withTestFixturesSourceSet(false)
                testSourceSetName { "${'$'}{it}Spec" }

                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("orders/src/domainSpec/java/com/example/orders/domain/OrderSpec.java")

            val validResult = gradleRunner.withArguments("validatePackageStructure").build()
            assertThat(validResult.task(":orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)

            // the test fixtures source set is disabled, so its folder is not a valid source location
            val unknownSourceSet =
                sourceFile("orders/src/domainTestFixtures/java/com/example/orders/domain/OrderFixtures.java")
            val invalidResult = gradleRunner.withArguments("validatePackageStructure").buildAndFail()
            assertThat(invalidResult.output)
                .contains("Source file '${unknownSourceSet.path}' does not belong to any source set.")
        }

        @Test
        fun `it should validate relocated source directories`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()
                disableFolderCreation()

                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            projectFolder.resolve("orders/build.gradle.kts").write(
                """
                customizeAllComponents {
                    productionSourceSet { sourceSet, componentName ->
                        sourceSet.java.setSrcDirs(listOf("${'$'}componentName/java"))
                    }
                }
                """
            )
            sourceFile("orders/domain/java/com/example/orders/domain/Order.java")

            val validResult = gradleRunner.withArguments("validatePackageStructure").build()
            assertThat(validResult.task(":orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)

            val invalidFile = sourceFile("orders/domain/java/com/example/orders/Order.java")
            val invalidResult = gradleRunner.withArguments("validatePackageStructure").buildAndFail()
            assertThat(invalidResult.output)
                .contains("Source file '${invalidFile.path}' has invalid package name.")
        }

        @Test
        fun `it should apply project name mappings to module and component names`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                    mapProjectNamesToPackageFragments(
                        "financial-accounting" to "accounting",
                        "webApi" to "web",
                    )
                }

                module("financial-accounting").createComponent("webApi")
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("financial-accounting/src/webApi/java/com/example/accounting/web/Controller.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":financial-accounting:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should exclude resources and configured source folders`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                    excludeResourcesFolder()
                    excludeSrcFolders("generated")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("orders/src/domain/resources/config.properties")
            sourceFile("orders/src/generated/java/anything/Generated.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }
    }

    @Nested
    inner class Settings {

        @Test
        fun `it should validate relocated source directories of component projects`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            projectFolder.resolve("orders/domain/build.gradle.kts").write(
                """
                sourceSets["main"].java.setSrcDirs(listOf("sources/java"))
                """
            )
            sourceFile("orders/domain/sources/java/com/example/orders/domain/Order.java")

            val validResult = gradleRunner.withArguments("validatePackageStructure").build()
            assertThat(validResult.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)

            val invalidFile = sourceFile("orders/domain/sources/java/com/example/orders/Order.java")
            val invalidResult = gradleRunner.withArguments("validatePackageStructure").buildAndFail()
            assertThat(invalidResult.output)
                .contains("Source file '${invalidFile.path}' has invalid package name.")
        }

        @Test
        fun `it should apply absolute package mappings to component projects`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                    mapProjectPathsToAbsolutePackages(":orders:domain" to "com.special")
                }

                module("orders").createComponent("domain")
                """
            )
            sourceFile("orders/domain/src/main/kotlin/com/special/Order.kt")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should skip excluded projects`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                    excludeProjectPath(":orders:domain")
                }

                module("orders").createComponent("domain")
                """
            )
            sourceFile("orders/domain/src/main/kotlin/wrong/Order.kt")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SKIPPED)
        }

        @Test
        fun `it should keep names unnormalized when normalization is disabled`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                    disableUseLowerCase()
                    disableRemoveHyphen()
                    disableRemoveUnderscore()
                }

                module("File_Store").createComponent("web-api")
                """
            )
            sourceFile("File_Store/web-api/src/main/kotlin/com/example/File_Store/web-api/Store.kt")
            val invalidFile = sourceFile("File_Store/web-api/src/test/kotlin/com/example/filestore/webapi/Store.kt")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.output).contains("Source file '${invalidFile.path}' has invalid package name.")
            assertThat(result.output).doesNotContain("File_Store${File.separator}web-api${File.separator}src${File.separator}main")
        }
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

    private fun sourceFile(relativePath: String): File {
        val file = projectFolder.resolve(relativePath)
        file.write("// content is irrelevant — the validation only checks the folder structure")
        return file
    }

    private fun File.write(content: String) {
        parentFile.mkdirs()
        writeText(content.trimIndent())
    }
}
