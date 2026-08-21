package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.BundleModule
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.core.model.component
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.util.projectDependencyConfigurations
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GradleProjectConfigApplicatorTest {

    @Nested
    inner class SourceSetSettingsTest {

        @Test
        fun `it should fall back to global source set settings`() {
            val project = javaProject()
            val settings = settings {
                withTestSourceSet(false)
                withTestFixturesSourceSet(false)
            }

            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings)

            assertThat(project.sourceSets().names).contains("domain")
            assertThat(project.sourceSets().names).doesNotContain("domainTest", "domainTestFixtures")
        }

        @Test
        fun `it should prefer component source set settings over global settings`() {
            val project = javaProject()
            val settings = settings {
                withTestSourceSet(false)
                withTestFixturesSourceSet(false)
            }

            val module = domainModule(
                component(
                    reference = ComponentReference("domain"),
                    withTestSourceSet = true,
                    withTestFixturesSourceSet = true,
                )
            )
            project.applyModuleConfig(module, settings)

            assertThat(project.sourceSets().names).contains("domainTest", "domainTestFixtures")
        }

        @Test
        fun `it should let a component opt out of the enabled global source set settings`() {
            val project = javaProject()

            val module = domainModule(
                component(
                    reference = ComponentReference("domain"),
                    withTestSourceSet = false,
                    withTestFixturesSourceSet = false,
                )
            )
            project.applyModuleConfig(module, settings())

            assertThat(project.sourceSets().names).contains("domain")
            assertThat(project.sourceSets().names).doesNotContain("domainTest", "domainTestFixtures")
        }

        @Test
        fun `it should apply each global source set flag on its own`() {
            val project = javaProject()
            val settings = settings { withTestFixturesSourceSet(false) }

            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings)

            assertThat(project.sourceSets().names).contains("domainTest")
            assertThat(project.sourceSets().names).doesNotContain("domainTestFixtures")
        }

        @Test
        fun `it should name test source sets with the global name functions`() {
            val project = javaProject()
            val settings = settings {
                testSourceSetName { "test-$it" }
                testFixturesSourceSetName { "fixtures-$it" }
            }

            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings)

            assertThat(project.sourceSets().names).contains("test-domain", "fixtures-domain")
            assertThat(project.sourceSets().names).doesNotContain("domainTest", "domainTestFixtures")
        }
    }

    @Nested
    inner class ConsumableComponentTest {

        @Test
        fun `it should make all components consumable`() {
            val project = javaProject()
            val module = domainModule(
                component(reference = ComponentReference("domain")),
                component(reference = ComponentReference("api")),
            )

            project.applyModuleConfig(module, settings())

            assertThat(project.configurations.getByName("domainApiElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("domainRuntimeElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("domainTestFixturesApiElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("domainTestFixturesRuntimeElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("apiApiElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("apiRuntimeElements").isCanBeConsumed).isTrue()
        }
    }

    @Nested
    inner class BundleModuleTest {

        @Test
        fun `it should depend on every component of an included source set module`() {
            val root = ProjectBuilder.builder().withName("root").build()
            val moduleProject = javaProject(root = root)
            val bundleProject = javaProject(name = "bundle", root = root)
            val module = domainModule(
                component(reference = ComponentReference("domain")),
                component(reference = ComponentReference("api")),
            )
            val bundle = BundleModule(
                reference = ModuleReference(name = "bundle"),
                plugin = null,
                includes = setOf(module.reference),
            )
            val applicator = GradleProjectConfigApplicator(
                settings(),
                listOf(
                    GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf("module")), module),
                    GradleBundleModuleProjectConfig(GradleProjectPath.of(listOf("bundle")), bundle),
                ),
            )

            applicator.applyConfig(moduleProject)
            applicator.applyConfig(bundleProject)

            assertThat(bundleProject.configurations.getByName("implementation").projectDependencyConfigurations())
                .containsExactlyInAnyOrder("domainApiElements", "apiApiElements")
            assertThat(bundleProject.configurations.getByName("runtimeOnly").projectDependencyConfigurations())
                .containsExactlyInAnyOrder("domainRuntimeElements", "apiRuntimeElements")
        }
    }

    private fun javaProject(
        name: String = "module",
        root: Project = ProjectBuilder.builder().withName("root").build(),
    ): Project {
        return ProjectBuilder.builder().withName(name).withParent(root).build()
            .also { it.pluginManager.apply("java-library") }
    }

    private fun settings(configure: ArciphantDsl.() -> Unit = {}) = CoreSettingsRepository(
        ArciphantDsl().apply {
            sourceSetComponentLayout()
            configure()
        }
    ).load()

    private fun domainModule(vararg components: Component) = DomainModule(
        reference = ModuleReference(name = "module"),
        components = components.toSet(),
    )

    private fun Project.applyModuleConfig(module: FunctionalModule, settings: CoreSettings) =
        GradleProjectConfigApplicator(
            settings,
            listOf(GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf(name)), module)),
        ).applyConfig(this)

}
