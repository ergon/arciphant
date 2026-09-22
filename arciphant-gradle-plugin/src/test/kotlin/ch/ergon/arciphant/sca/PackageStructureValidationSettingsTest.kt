package ch.ergon.arciphant.sca

import ch.ergon.arciphant.util.dynamicTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class PackageStructureValidationSettingsTest {

    @Nested
    inner class DeterminePackageFor {

        @TestFactory
        fun `it should map project paths with the default normalization`() = dynamicTest(
            ":module:component" to "com/example/module/component",
            ":Financial_Accounting" to "com/example/financialaccounting",
            ":web-api" to "com/example/webapi",
        ) { projectPath -> settings().determinePackageFor(projectPath) }

        @Test
        fun `it should omit the base package when none is configured`() {
            assertThat(settings(basePackagePath = null).determinePackageFor(":module:component"))
                .isEqualTo("module/component")
        }

        @Test
        fun `it should append the component fragment to the project package`() {
            assertThat(settings().determinePackageFor(":module", componentName = "webApi"))
                .isEqualTo("com/example/module/webapi")
        }

        @Test
        fun `it should apply name mappings to module names`() {
            val settings = settings(
                relativePackagePathsByProjectName = mapOf("financial-accounting" to "accounting"),
            )
            assertThat(settings.determinePackageFor(":financial-accounting:web"))
                .isEqualTo("com/example/accounting/web")
        }

        @Test
        fun `it should apply name mappings to component names`() {
            val settings = settings(
                relativePackagePathsByProjectName = mapOf("payment-provider-adapter" to "ppa"),
            )
            assertThat(settings.determinePackageFor(":accounting", componentName = "payment-provider-adapter"))
                .isEqualTo("com/example/accounting/ppa")
        }

        @Test
        fun `it should skip a component fragment that is mapped to an empty string`() {
            val settings = settings(relativePackagePathsByProjectName = mapOf("api" to ""))
            assertThat(settings.determinePackageFor(":module", componentName = "api"))
                .isEqualTo("com/example/module")
        }

        @Test
        fun `it should apply absolute package mappings`() {
            val settings = settings(absolutePackagePathsByProjectPath = mapOf(":module" to "com/special"))
            assertThat(settings.determinePackageFor(":module")).isEqualTo("com/special")
        }

        @Test
        fun `it should append the component fragment to an absolute package mapping`() {
            val settings = settings(absolutePackagePathsByProjectPath = mapOf(":module" to "com/special"))
            assertThat(settings.determinePackageFor(":module", componentName = "domain"))
                .isEqualTo("com/special/domain")
        }

        @Test
        fun `it should keep upper case letters when lower casing is disabled`() {
            assertThat(settings(useLowerCase = false).determinePackageFor(":FileStore"))
                .isEqualTo("com/example/FileStore")
        }

        @Test
        fun `it should keep special characters when their removal is disabled`() {
            val settings = settings(removedSpecialCharacters = emptySet())
            assertThat(settings.determinePackageFor(":file_store", componentName = "web-api"))
                .isEqualTo("com/example/file_store/web-api")
        }
    }

    @Nested
    inner class DetermineValidSourceFolderPatterns {

        @Test
        fun `it should validate every source set against the project package without component source sets`() {
            assertThat(settings().determineValidSourceFolderPatterns(":module:component", componentSourceSets = null))
                .containsExactly("src/*/*/com/example/module/component/**")
        }

        @Test
        fun `it should validate component source sets against the component package`() {
            val componentSourceSets = listOf(
                ComponentSourceSets("domain", listOf("domain", "domainTest", "domainTestFixtures")),
                ComponentSourceSets("webApi", listOf("webApi")),
            )
            assertThat(settings().determineValidSourceFolderPatterns(":module", componentSourceSets))
                .containsExactlyInAnyOrder(
                    "src/main/*/com/example/module/**",
                    "src/test/*/com/example/module/**",
                    "src/domain/*/com/example/module/domain/**",
                    "src/domainTest/*/com/example/module/domain/**",
                    "src/domainTestFixtures/*/com/example/module/domain/**",
                    "src/webApi/*/com/example/module/webapi/**",
                )
        }
    }

    private fun settings(
        basePackagePath: String? = "com/example",
        useLowerCase: Boolean = true,
        removedSpecialCharacters: Set<String> = setOf("_", "-"),
        relativePackagePathsByProjectName: Map<String, String> = emptyMap(),
        absolutePackagePathsByProjectPath: Map<String, String> = emptyMap(),
    ) = PackageStructureValidationSettings(
        basePackagePath = basePackagePath,
        useLowerCase = useLowerCase,
        removedSpecialCharacters = removedSpecialCharacters,
        relativePackagePathsByProjectName = relativePackagePathsByProjectName,
        absolutePackagePathsByProjectPath = absolutePackagePathsByProjectPath,
        excludedProjectPaths = emptySet(),
        excludedSrcFolders = emptySet(),
        excludeResourcesFolder = false,
    )
}
