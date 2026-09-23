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
        fun `it should apply module name mappings to module names`() {
            val settings = settings(
                packageFragmentsByModuleName = mapOf("financial-accounting" to "accounting"),
            )
            val (projectPath, project) = componentProject(":financial-accounting:web", "financial-accounting", "web")
            assertThat(settings.determinePackageFor(projectPath, project))
                .isEqualTo("com/example/accounting/web")
        }

        @Test
        fun `it should apply component name mappings to component names`() {
            val settings = settings(
                packageFragmentsByComponentName = mapOf("payment-provider-adapter" to "ppa"),
            )
            val (projectPath, project) = moduleProject(":accounting", "accounting")
            assertThat(settings.determinePackageFor(projectPath, project, componentName = "payment-provider-adapter"))
                .isEqualTo("com/example/accounting/ppa")
        }

        @Test
        fun `it should not apply module name mappings to components and vice versa`() {
            val settings = settings(
                packageFragmentsByModuleName = mapOf("api" to "moduleapi"),
                packageFragmentsByComponentName = mapOf("api" to "componentapi"),
            )
            val (projectPath, project) = componentProject(":api:api", "api", "api")
            assertThat(settings.determinePackageFor(projectPath, project))
                .isEqualTo("com/example/moduleapi/componentapi")
        }

        @Test
        fun `it should not apply name mappings to base path segments`() {
            val settings = settings(packageFragmentsByModuleName = mapOf("backend" to "b"))
            val (projectPath, project) = moduleProject(":backend:orders", "orders", basePath = listOf("backend"))
            assertThat(settings.determinePackageFor(projectPath, project))
                .isEqualTo("com/example/backend/orders")
        }

        @Test
        fun `it should not apply name mappings to unmanaged projects`() {
            val settings = settings(packageFragmentsByModuleName = mapOf("tooling" to "t"))
            assertThat(settings.determinePackageFor(":tooling", project = null))
                .isEqualTo("com/example/tooling")
        }

        @Test
        fun `it should skip a component fragment that is mapped to an empty string`() {
            val settings = settings(packageFragmentsByComponentName = mapOf("api" to ""))
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
        fun `it should apply an absolute package mapping of the module path to its component projects`() {
            val settings = settings(absolutePackagePathsByProjectPath = mapOf(":module" to "com/special"))
            val (projectPath, project) = componentProject(":module:web-api", "module", "web-api")
            assertThat(settings.determinePackageFor(projectPath, project)).isEqualTo("com/special/webapi")
        }

        @Test
        fun `it should prefer an absolute package mapping of the component project over the one of its module`() {
            val settings = settings(
                absolutePackagePathsByProjectPath = mapOf(":module" to "com/special", ":module:domain" to "com/other"),
            )
            val (projectPath, project) = componentProject(":module:domain", "module", "domain")
            assertThat(settings.determinePackageFor(projectPath, project)).isEqualTo("com/other")
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

    private fun moduleProject(
        projectPath: String,
        moduleName: String,
        basePath: List<String> = emptyList(),
    ): Pair<String, ValidatedProject> = projectPath to ValidatedProject(
        basePathFragments = basePath,
        moduleName = moduleName,
        modulePath = projectPath,
        componentName = null,
        componentSourceSets = null,
    )

    private fun componentProject(
        projectPath: String,
        moduleName: String,
        componentName: String,
        basePath: List<String> = emptyList(),
    ): Pair<String, ValidatedProject> = projectPath to ValidatedProject(
        basePathFragments = basePath,
        moduleName = moduleName,
        modulePath = projectPath.substringBeforeLast(":"),
        componentName = componentName,
        componentSourceSets = null,
    )

    private fun settings(
        basePackagePath: String? = "com/example",
        useLowerCase: Boolean = true,
        removedSpecialCharacters: Set<String> = setOf("_", "-"),
        packageFragmentsByModuleName: Map<String, String> = emptyMap(),
        packageFragmentsByComponentName: Map<String, String> = emptyMap(),
        absolutePackagePathsByProjectPath: Map<String, String> = emptyMap(),
    ) = PackageStructureValidationSettings(
        basePackagePath = basePackagePath,
        useLowerCase = useLowerCase,
        removedSpecialCharacters = removedSpecialCharacters,
        packageFragmentsByModuleName = packageFragmentsByModuleName,
        packageFragmentsByComponentName = packageFragmentsByComponentName,
        absolutePackagePathsByProjectPath = absolutePackagePathsByProjectPath,
        excludedProjectPaths = emptySet(),
        excludedSrcFolders = emptySet(),
        excludeResourcesFolder = false,
    )
}
