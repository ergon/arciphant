package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.LibraryModule
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.core.model.component
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
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
        fun `it should make library components consumable`() {
            val project = javaProject(name = "library")
            val module = LibraryModule(
                reference = ModuleReference(name = "library"),
                components = setOf(component(reference = ComponentReference("domain"), consumable = false)),
            )

            project.applyModuleConfig(module, settings())

            assertThat(project.configurations.getByName("domainApiElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("domainRuntimeElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("domainTestFixturesApiElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("domainTestFixturesRuntimeElements").isCanBeConsumed).isTrue()
        }

        @Test
        fun `it should make functional module components consumable only when configured`() {
            val project = javaProject()
            val module = domainModule(
                component(reference = ComponentReference("domain")),
                component(reference = ComponentReference("api"), consumable = true),
            )

            project.applyModuleConfig(module, settings())

            assertThat(project.configurations.findByName("domainApiElements")).isNull()
            assertThat(project.configurations.findByName("domainRuntimeElements")).isNull()
            assertThat(project.configurations.getByName("apiApiElements").isCanBeConsumed).isTrue()
            assertThat(project.configurations.getByName("apiRuntimeElements").isCanBeConsumed).isTrue()
        }
    }

    private fun javaProject(name: String = "module"): Project {
        val root = ProjectBuilder.builder().withName("root").build()
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
