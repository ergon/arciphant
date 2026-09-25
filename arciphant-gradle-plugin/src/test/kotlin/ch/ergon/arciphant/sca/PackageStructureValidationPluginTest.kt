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
            buildFileWithJvmPlugins()
            sourceFile("orders/domain/src/main/java/com/example/orders/domain/Order.java")
            sourceFile("orders/domain/src/main/java/com/example/orders/domain/sub/Nested.java")
            sourceFile("orders/domain/src/test/java/com/example/orders/domain/OrderTest.java")
            sourceFile("orders/web-api/src/main/java/com/example/orders/webapi/OrderController.java")

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
            buildFileWithJvmPlugins()
            val wrongModule = sourceFile("orders/domain/src/main/java/com/example/other/domain/Order.java")
            val missingComponent = sourceFile("orders/web/src/main/java/com/example/orders/OrderController.java")

            val result = gradleRunner.withArguments("validatePackageStructure", "--continue").buildAndFail()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.FAILED)
            assertThat(result.task(":orders:web:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.FAILED)
            assertThat(result.output).contains("Source file '${wrongModule.path}' has invalid package name.")
            assertThat(result.output).contains("Source file '${missingComponent.path}' has invalid package name.")
        }

        @Test
        fun `it should validate resources folders by default`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            val validFile =
                sourceFile("orders/domain/src/main/resources/com/example/orders/domain/messages.properties")
            val invalidFile = sourceFile("orders/domain/src/main/resources/config.properties")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.output).contains("Source file '${invalidFile.path}' has invalid package name.")
            assertThat(result.output).doesNotContain("Source file '${validFile.path}'")
        }

        @Test
        fun `it should validate test fixtures source sets`() {
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
                plugins {
                    `java-test-fixtures`
                }
                """
            )
            val validFile = sourceFile("orders/domain/src/testFixtures/java/com/example/orders/domain/Fixture.java")
            val invalidFile = sourceFile("orders/domain/src/testFixtures/java/com/example/orders/Fixture.java")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.output).contains("Source file '${invalidFile.path}' has invalid package name.")
            assertThat(result.output).doesNotContain("Source file '${validFile.path}'")
        }

        @Test
        fun `it should skip generated source directories below the build directory`() {
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
                sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/sources"))
                """
            )
            sourceFile("orders/domain/build/generated/sources/anything/Generated.java")
            sourceFile("orders/domain/src/main/java/com/example/orders/domain/Order.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should validate the intermediate module project`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                    mapModuleNamesToPackageFragments("financial-orders" to "orders")
                }

                module("financial-orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            val validFile = sourceFile("financial-orders/src/main/java/com/example/orders/Shared.java")
            val invalidFile = sourceFile("financial-orders/src/main/java/com/example/other/Shared.java")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.task(":financial-orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.FAILED)
            assertThat(result.output).contains("Source file '${invalidFile.path}' has invalid package name.")
            assertThat(result.output).doesNotContain("Source file '${validFile.path}'")
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
        fun `it should apply module and component name mappings`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                    mapModuleNamesToPackageFragments("financial-accounting" to "accounting")
                    mapComponentNamesToPackageFragments("webApi" to "web")
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
        fun `it should validate Kotlin source directories`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFile.write(
                """
                plugins {
                    kotlin("jvm") version "2.2.0" apply false
                }

                allprojects {
                    pluginManager.apply("org.jetbrains.kotlin.jvm")
                    repositories { mavenCentral() }
                }
                """
            )
            val validFile = sourceFile("orders/src/domain/kotlin/com/example/orders/domain/Order.kt")
            val invalidFile = sourceFile("orders/src/domain/kotlin/com/example/orders/Order.kt")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.output).contains("Source file '${invalidFile.path}' has invalid package name.")
            assertThat(result.output).doesNotContain("Source file '${validFile.path}'")
        }

        @Test
        fun `it should skip excluded module projects`() {
            settingsFileWithArciphant(
                """
                sourceSetComponentLayout()

                packageStructureValidation {
                    basePackageName("com.example")
                    excludeProjectPath(":orders")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("orders/src/domain/java/wrong/Order.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SKIPPED)
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
            buildFileWithJvmPlugins()
            sourceFile("orders/domain/src/main/java/com/special/Order.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should apply absolute package mappings of a module to its component projects`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                    mapProjectPathsToAbsolutePackages(":orders" to "com.special")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("orders/domain/src/main/java/com/special/domain/Order.java")

            val validResult = gradleRunner.withArguments("validatePackageStructure").build()
            assertThat(validResult.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)

            val invalidFile = sourceFile("orders/domain/src/main/java/com/example/orders/domain/Invoice.java")
            val invalidResult = gradleRunner.withArguments("validatePackageStructure").buildAndFail()
            assertThat(invalidResult.output)
                .contains("Source file '${invalidFile.path}' has invalid package name.")
        }

        @Test
        fun `it should not apply name mappings to base path segments`() {
            settingsFileWithArciphant(
                """
                basePath("backend")

                packageStructureValidation {
                    basePackageName("com.example")
                    mapModuleNamesToPackageFragments("backend" to "short")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            sourceFile("backend/orders/domain/src/main/java/com/example/backend/orders/domain/Order.java")

            val validResult = gradleRunner.withArguments("validatePackageStructure").build()
            assertThat(validResult.task(":backend:orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)

            val invalidFile =
                sourceFile("backend/orders/domain/src/main/java/com/example/short/orders/domain/Order.java")
            val invalidResult = gradleRunner.withArguments("validatePackageStructure").buildAndFail()
            assertThat(invalidResult.output)
                .contains("Source file '${invalidFile.path}' has invalid package name.")
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
        fun `it should exclude configured src folders anywhere below src`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                    excludeSrcFolders("main/generated")
                    excludeSrcFolders("main/java/generated")
                }

                module("orders").createComponent("domain")
                """
            )
            buildFileWithJvmPlugins()
            // a folder next to the source directories …
            sourceFile("orders/domain/src/main/generated/Generated.java")
            // … and one inside a source directory
            sourceFile("orders/domain/src/main/java/generated/Generated.java")

            val result = gradleRunner.withArguments("validatePackageStructure").build()

            assertThat(result.task(":orders:domain:validatePackageStructure")?.outcome)
                .isEqualTo(TaskOutcome.SUCCESS)
        }

        @Test
        fun `it should report files of projects without source sets`() {
            settingsFileWithArciphant(
                """
                packageStructureValidation {
                    basePackageName("com.example")
                }

                module("orders").createComponent("domain")
                """
            )
            // no JVM plugin applied — the project has no source sets, so no source location is valid
            val file = sourceFile("orders/domain/src/main/java/com/example/orders/domain/Order.java")

            val result = gradleRunner.withArguments("validatePackageStructure").buildAndFail()

            assertThat(result.output)
                .contains("Source file '${file.path}' does not belong to any source set.")
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
            buildFileWithJvmPlugins()
            sourceFile("File_Store/web-api/src/main/java/com/example/File_Store/web-api/Store.java")
            val invalidFile = sourceFile("File_Store/web-api/src/test/java/com/example/filestore/webapi/Store.java")

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
