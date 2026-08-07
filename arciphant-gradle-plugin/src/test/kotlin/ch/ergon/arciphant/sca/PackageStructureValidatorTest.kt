package ch.ergon.arciphant.sca

import ch.ergon.arciphant.dsl.ArciphantProjectDsl
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PackageStructureValidatorTest {

    @TempDir
    private lateinit var rootFolder: File

    private val rootProject: Project by lazy {
        ProjectBuilder.builder().withProjectDir(rootFolder).build()
    }

    private val accounting by lazy { rootProject.subproject("accounting") }
    private val paymentProviderAdapter by lazy { accounting.subproject("payment-provider-adapter") }

    @Nested
    inner class SettingsLevelConfiguration {

        @Test
        fun `it should accept source files in the package derived from the project path`() {
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/paymentprovideradapter/PaymentProvider.kt")

            val result = PackageStructureValidator(settings()).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should report source files outside the expected package`() {
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/wrong/PaymentProvider.kt")

            val result = PackageStructureValidator(settings()).validate(paymentProviderAdapter)

            assertThat(result).isInstanceOf(InvalidPackageStructure::class.java)
            assertThat((result as InvalidPackageStructure).invalidFiles.map { it.name }).containsExactly("PaymentProvider.kt")
        }

        @Test
        fun `it should prepend the base package`() {
            paymentProviderAdapter.sourceFile("src/main/kotlin/com/acme/accounting/paymentprovideradapter/PaymentProvider.kt")

            val result = PackageStructureValidator(settings(basePackagePath = "com/acme")).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should apply project name mappings`() {
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/ppa/PaymentProvider.kt")

            val result = PackageStructureValidator(
                settings(relativePackagePathsByProjectName = mapOf("payment-provider-adapter" to "ppa"))
            ).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should apply absolute package mappings without the base package`() {
            paymentProviderAdapter.sourceFile("src/main/kotlin/com/other/PaymentProvider.kt")

            val result = PackageStructureValidator(
                settings(
                    basePackagePath = "com/acme",
                    absolutePackagePathsByProjectPath = mapOf(":accounting:payment-provider-adapter" to "com/other"),
                )
            ).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }
    }

    @Nested
    inner class ProjectLevelConfiguration {

        @Test
        fun `it should apply a package name configured on the project itself`() {
            paymentProviderAdapter.withArciphant { packageName = "ppa" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/com/acme/accounting/ppa/PaymentProvider.kt")

            val result = PackageStructureValidator(settings(basePackagePath = "com/acme")).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should apply a package name configured on a parent project`() {
            accounting.withArciphant { packageName = "acc" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/acc/paymentprovideradapter/PaymentProvider.kt")

            val result = PackageStructureValidator(settings()).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should drop the package fragment for an empty package name`() {
            accounting.withArciphant { packageName = "" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/paymentprovideradapter/PaymentProvider.kt")

            val result = PackageStructureValidator(settings()).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should apply an absolute package name without the base package`() {
            paymentProviderAdapter.withArciphant { absolutePackageName = "com.other" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/com/other/PaymentProvider.kt")

            val result = PackageStructureValidator(settings(basePackagePath = "com/acme")).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should not apply an absolute package name to subprojects`() {
            accounting.withArciphant { absolutePackageName = "com.other" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/paymentprovideradapter/PaymentProvider.kt")

            val result = PackageStructureValidator(settings()).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should prefer the project package name over settings project name mappings`() {
            paymentProviderAdapter.withArciphant { packageName = "ppa" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/ppa/PaymentProvider.kt")

            val result = PackageStructureValidator(
                settings(relativePackagePathsByProjectName = mapOf("payment-provider-adapter" to "fromsettings"))
            ).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should prefer the project absolute package name over settings absolute package mappings`() {
            paymentProviderAdapter.withArciphant { absolutePackageName = "com.other" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/com/other/PaymentProvider.kt")

            val result = PackageStructureValidator(
                settings(absolutePackagePathsByProjectPath = mapOf(":accounting:payment-provider-adapter" to "com/fromsettings"))
            ).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should ignore settings absolute package mappings if the project configures a package name`() {
            paymentProviderAdapter.withArciphant { packageName = "ppa" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/ppa/PaymentProvider.kt")

            val result = PackageStructureValidator(
                settings(absolutePackagePathsByProjectPath = mapOf(":accounting:payment-provider-adapter" to "com/fromsettings"))
            ).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should fall back to the default behavior if no values are configured`() {
            paymentProviderAdapter.withArciphant { }
            paymentProviderAdapter.sourceFile("src/main/kotlin/accounting/paymentprovideradapter/PaymentProvider.kt")

            val result = PackageStructureValidator(settings()).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }

        @Test
        fun `it should not validate excluded projects`() {
            paymentProviderAdapter.withArciphant { packageName = "ppa" }
            paymentProviderAdapter.sourceFile("src/main/kotlin/totally/wrong/PaymentProvider.kt")

            val result = PackageStructureValidator(
                settings(excludedProjectPaths = setOf(":accounting:payment-provider-adapter"))
            ).validate(paymentProviderAdapter)

            assertThat(result).isEqualTo(ValidPackageStructure)
        }
    }

    private fun Project.subproject(name: String): Project = ProjectBuilder.builder()
        .withParent(this)
        .withName(name)
        .withProjectDir(projectDir.resolve(name))
        .build()

    private fun Project.withArciphant(configure: ArciphantProjectDsl.() -> Unit) {
        extensions.create("arciphant", ArciphantProjectDsl::class.java).configure()
    }

    private fun Project.sourceFile(relativePath: String) {
        projectDir.resolve(relativePath).apply {
            parentFile.mkdirs()
            writeText("// test source file")
        }
    }

    private fun settings(
        basePackagePath: String? = null,
        useLowerCase: Boolean = true,
        removedSpecialCharacters: Set<String> = setOf("_", "-"),
        relativePackagePathsByProjectName: Map<String, String> = emptyMap(),
        absolutePackagePathsByProjectPath: Map<String, String> = emptyMap(),
        excludedProjectPaths: Set<String> = emptySet(),
        excludedSourceFolders: Set<String> = emptySet(),
    ) = PackageStructureValidationSettings(
        basePackagePath = basePackagePath,
        useLowerCase = useLowerCase,
        removedSpecialCharacters = removedSpecialCharacters,
        relativePackagePathsByProjectName = relativePackagePathsByProjectName,
        absolutePackagePathsByProjectPath = absolutePackagePathsByProjectPath,
        excludedProjectPaths = excludedProjectPaths,
        excludedSourceFolders = excludedSourceFolders,
    )

}
