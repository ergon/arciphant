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
        fun `it should normalize module and component names by default`() = dynamicTest(
            componentProject(":module:component", "module", "component") to "com/example/module/component",
            moduleProject(":Financial_Accounting", "Financial_Accounting") to "com/example/financialaccounting",
            componentProject(":orders:web-api", "orders", "web-api") to "com/example/orders/webapi",
        ) { (projectPath, project) -> settings().determinePackageFor(projectPath, project) }

        @Test
        fun `it should normalize base path segments`() {
            val project = moduleProject(":Back_End:orders", "orders", basePath = listOf("Back_End")).second
            assertThat(settings().determinePackageFor(":Back_End:orders", project))
                .isEqualTo("com/example/backend/orders")
        }

        @Test
        fun `it should omit the base package when none is configured`() {
            val (projectPath, project) = componentProject(":module:component", "module", "component")
            assertThat(settings(basePackagePath = null).determinePackageFor(projectPath, project))
                .isEqualTo("module/component")
        }

        @Test
        fun `it should append the component fragment to the module package`() {
            val (projectPath, project) = moduleProject(":module", "module")
            assertThat(settings().determinePackageFor(projectPath, project, componentName = "webApi"))
                .isEqualTo("com/example/module/webapi")
        }

        @Test
        fun `it should apply name mappings to module names`() {
            val settings = settings(
                relativePackagePathsByProjectName = mapOf("financial-accounting" to "accounting"),
            )
            val (projectPath, project) = componentProject(":financial-accounting:web", "financial-accounting", "web")
            assertThat(settings.determinePackageFor(projectPath, project))
                .isEqualTo("com/example/accounting/web")
        }

        @Test
        fun `it should apply name mappings to component names`() {
            val settings = settings(
                relativePackagePathsByProjectName = mapOf("payment-provider-adapter" to "ppa"),
            )
            val (projectPath, project) = moduleProject(":accounting", "accounting")
            assertThat(settings.determinePackageFor(projectPath, project, componentName = "payment-provider-adapter"))
                .isEqualTo("com/example/accounting/ppa")
        }

        @Test
        fun `it should not apply name mappings to base path segments`() {
            val settings = settings(relativePackagePathsByProjectName = mapOf("backend" to "b"))
            val (projectPath, project) = moduleProject(":backend:orders", "orders", basePath = listOf("backend"))
            assertThat(settings.determinePackageFor(projectPath, project))
                .isEqualTo("com/example/backend/orders")
        }

        @Test
        fun `it should not apply name mappings to unmanaged projects`() {
            val settings = settings(relativePackagePathsByProjectName = mapOf("tooling" to "t"))
            assertThat(settings.determinePackageFor(":tooling", project = null))
                .isEqualTo("com/example/tooling")
        }

        @Test
        fun `it should skip a component fragment that is mapped to an empty string`() {
            val settings = settings(relativePackagePathsByProjectName = mapOf("api" to ""))
            val (projectPath, project) = moduleProject(":module", "module")
            assertThat(settings.determinePackageFor(projectPath, project, componentName = "api"))
                .isEqualTo("com/example/module")
        }

        @Test
        fun `it should apply absolute package mappings`() {
            val settings = settings(absolutePackagePathsByProjectPath = mapOf(":module" to "com/special"))
            val (projectPath, project) = moduleProject(":module", "module")
            assertThat(settings.determinePackageFor(projectPath, project)).isEqualTo("com/special")
        }

        @Test
        fun `it should append the component fragment to an absolute package mapping`() {
            val settings = settings(absolutePackagePathsByProjectPath = mapOf(":module" to "com/special"))
            val (projectPath, project) = moduleProject(":module", "module")
            assertThat(settings.determinePackageFor(projectPath, project, componentName = "domain"))
                .isEqualTo("com/special/domain")
        }

        @Test
        fun `it should keep upper case letters when lower casing is disabled`() {
            val (projectPath, project) = moduleProject(":FileStore", "FileStore")
            assertThat(settings(useLowerCase = false).determinePackageFor(projectPath, project))
                .isEqualTo("com/example/FileStore")
        }

        @Test
        fun `it should keep special characters when their removal is disabled`() {
            val settings = settings(removedSpecialCharacters = emptySet())
            val (projectPath, project) = moduleProject(":file_store", "file_store")
            assertThat(settings.determinePackageFor(projectPath, project, componentName = "web-api"))
                .isEqualTo("com/example/file_store/web-api")
        }
    }

    @Nested
    inner class DetermineValidSourceFolderPatterns {

        @Test
        fun `it should validate every source set against the project package without component source sets`() {
            val (projectPath, project) = componentProject(":module:component", "module", "component")
            assertThat(settings().determineValidSourceFolderPatterns(projectPath, project))
                .containsExactly("src/*/*/com/example/module/component/**")
        }

        @Test
        fun `it should validate component source sets against the component package`() {
            val project = ValidatedProject(
                basePathFragments = emptyList(),
                mappableNames = listOf("module"),
                componentSourceSets = listOf(
                    ComponentSourceSets("domain", listOf("domain", "domainTest", "domainTestFixtures")),
                    ComponentSourceSets("webApi", listOf("webApi")),
                ),
            )
            assertThat(settings().determineValidSourceFolderPatterns(":module", project))
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

    private fun moduleProject(
        projectPath: String,
        moduleName: String,
        basePath: List<String> = emptyList(),
    ): Pair<String, ValidatedProject> = projectPath to ValidatedProject(
        basePathFragments = basePath,
        mappableNames = listOf(moduleName),
        componentSourceSets = null,
    )

    private fun componentProject(
        projectPath: String,
        moduleName: String,
        componentName: String,
        basePath: List<String> = emptyList(),
    ): Pair<String, ValidatedProject> = projectPath to ValidatedProject(
        basePathFragments = basePath,
        mappableNames = listOf(moduleName, componentName),
        componentSourceSets = null,
    )

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
