package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.GlobalSettingsRepository
import ch.ergon.arciphant.core.GradleFunctionalModuleProjectConfig
import ch.ergon.arciphant.core.GradleProjectPath
import ch.ergon.arciphant.core.model.BundleModule
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.Dependency
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.LibraryModule
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.core.model.Plugin
import ch.ergon.arciphant.core.model.component
import ch.ergon.arciphant.core.toProjectConfigs
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.util.configuration
import ch.ergon.arciphant.util.projectDependencyPaths
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProjectLayoutConfigApplicatorTest {

    private val root = ProjectBuilder.builder().withName("root").build()

    @Nested
    inner class ComponentDependencyTest {

        @Test
        fun `it should add an implementation dependency between components`() {
            val module = domainModule(
                component(ComponentReference("api")),
                component(
                    reference = ComponentReference("domain"),
                    dependsOn = setOf(Dependency(component = ComponentReference("api"), type = IMPLEMENTATION)),
                ),
            )
            val domainProject = javaProject(":module:domain")
            project(":module:api")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.configuration("implementation").projectDependencyPaths())
                .containsExactly(":module:api")
        }

        @Test
        fun `it should add an api dependency between components`() {
            val module = domainModule(
                component(ComponentReference("api")),
                component(
                    reference = ComponentReference("domain"),
                    dependsOn = setOf(Dependency(component = ComponentReference("api"), type = API)),
                ),
            )
            val domainProject = javaProject(":module:domain")
            project(":module:api")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.configuration("api").projectDependencyPaths())
                .containsExactly(":module:api")
        }
    }

    @Nested
    inner class LibraryDependencyTest {

        @Test
        fun `it should add an api dependency from a domain component to the same-named library component`() {
            val library = libraryModule(
                component(ComponentReference("domain")),
                component(ComponentReference("other")),
            )
            val module = domainModule(component(ComponentReference("domain")))
            val domainProject = javaProject(":module:domain")
            project(":library:domain")
            project(":library:other")

            applicator(module, library).applyConfig(domainProject)

            assertThat(domainProject.configuration("api").projectDependencyPaths())
                .containsExactly(":library:domain")
        }

        @Test
        fun `it should not add library dependencies to a library module component`() {
            val library = libraryModule(component(ComponentReference("domain")))
            val libraryProject = javaProject(":library:domain")

            applicator(library).applyConfig(libraryProject)

            assertThat(libraryProject.configuration("api").projectDependencyPaths()).isEmpty()
        }
    }

    @Nested
    inner class PluginTest {

        @Test
        fun `it should apply the component plugin`() {
            val module = domainModule(
                component(reference = ComponentReference("domain"), plugin = Plugin(id = "jacoco")),
            )
            val domainProject = javaProject(":module:domain")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.pluginManager.hasPlugin("jacoco")).isTrue()
        }

        @Test
        fun `it should apply the bundle plugin`() {
            val bundle = bundleModule(plugin = Plugin(id = "jacoco"))
            val bundleProject = javaProject(":bundle")

            applicator(bundle).applyConfig(bundleProject)

            assertThat(bundleProject.pluginManager.hasPlugin("jacoco")).isTrue()
        }
    }

    @Nested
    inner class TestFixturesTest {

        @Test
        fun `it should mirror an api component dependency to the api scope of the test fixtures`() {
            val module = moduleWithDomainDependingOnApi(type = API)
            val domainProject = javaProject(":module:domain")
            domainProject.pluginManager.apply("java-test-fixtures")
            project(":module:api")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.configuration("testFixturesApi").projectDependencyPaths())
                .contains(":module:api")
            assertThat(domainProject.configuration("testFixturesImplementation").projectDependencyPaths())
                .doesNotContain(":module:api")
        }

        @Test
        fun `it should mirror an implementation component dependency to the implementation scope of the test fixtures`() {
            val module = moduleWithDomainDependingOnApi(type = IMPLEMENTATION)
            val domainProject = javaProject(":module:domain")
            domainProject.pluginManager.apply("java-test-fixtures")
            project(":module:api")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.configuration("testFixturesImplementation").projectDependencyPaths())
                .contains(":module:api")
            assertThat(domainProject.configuration("testFixturesApi").projectDependencyPaths())
                .doesNotContain(":module:api")
        }

        @Test
        fun `it should give the tests access to the fixtures of an implementation component dependency`() {
            val module = moduleWithDomainDependingOnApi(type = IMPLEMENTATION)
            val domainProject = javaProject(":module:domain")
            domainProject.pluginManager.apply("java-test-fixtures")
            project(":module:api")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.configuration("testImplementation").projectDependencyPaths())
                .contains(":module:api")
        }

        @Test
        fun `it should defer the test fixtures dependency until the java-test-fixtures plugin is applied`() {
            val module = moduleWithDomainDependingOnApi(type = IMPLEMENTATION)
            val domainProject = javaProject(":module:domain")
            project(":module:api")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.configurations.findByName("testFixturesImplementation")).isNull()

            domainProject.pluginManager.apply("java-test-fixtures")

            assertThat(domainProject.configuration("testFixturesImplementation").projectDependencyPaths())
                .contains(":module:api")
        }

        private fun moduleWithDomainDependingOnApi(type: DependencyType) = domainModule(
            component(ComponentReference("api")),
            component(
                reference = ComponentReference("domain"),
                dependsOn = setOf(Dependency(component = ComponentReference("api"), type = type)),
            ),
        )
    }

    @Nested
    inner class ArchiveBaseNameTest {

        @Test
        fun `it should qualify the jar archive base name with the module name`() {
            val module = domainModule(component(ComponentReference("domain")))
            val domainProject = javaProject(":module:domain")

            applicator(module).applyConfig(domainProject)

            assertThat(domainProject.jarTask().archiveBaseName.get()).isEqualTo("module-domain")
        }

        @Test
        fun `it should keep the default archive base name when disabled`() {
            val module = domainModule(component(ComponentReference("domain")))
            val domainProject = javaProject(":module:domain")

            applicator(module) { disableQualifiedArchiveBaseName() }.applyConfig(domainProject)

            assertThat(domainProject.jarTask().archiveBaseName.get()).isEqualTo("domain")
        }

        private fun Project.jarTask() = tasks.withType(Jar::class.java).getByName("jar")
    }

    @Nested
    inner class BundleModuleTest {

        @Test
        fun `it should depend on every component project of an included module`() {
            val module = domainModule(
                component(ComponentReference("domain")),
                component(ComponentReference("api")),
            )
            val bundle = bundleModule(module.reference)
            val bundleProject = javaProject(":bundle")
            project(":module:domain")
            project(":module:api")

            applicator(module, bundle).applyConfig(bundleProject)

            assertThat(bundleProject.configuration("implementation").projectDependencyPaths())
                .containsExactlyInAnyOrder(":module:domain", ":module:api")
        }

        @Test
        fun `it should not depend on components of modules that are not included`() {
            val included = domainModule(component(ComponentReference("domain")), name = "included")
            val excluded = domainModule(component(ComponentReference("domain")), name = "excluded")
            val bundle = bundleModule(included.reference)
            val bundleProject = javaProject(":bundle")
            project(":included:domain")
            project(":excluded:domain")

            applicator(included, excluded, bundle).applyConfig(bundleProject)

            assertThat(bundleProject.configuration("implementation").projectDependencyPaths())
                .containsExactly(":included:domain")
        }
    }

    @Nested
    inner class ProjectConfigTest {

        @Test
        fun `it should ignore projects that are not managed by arciphant`() {
            val module = domainModule(component(ComponentReference("domain")))
            val otherProject = javaProject(":other")

            applicator(module).applyConfig(otherProject)

            assertThat(otherProject.configuration("implementation").dependencies).isEmpty()
        }

        @Test
        fun `it should fail for a functional module project`() {
            val module = domainModule(component(ComponentReference("domain")))
            val applicator = ProjectLayoutConfigApplicator(
                settings(),
                listOf(GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf("module")), module)),
            )
            val moduleProject = javaProject(":module")

            assertThatThrownBy { applicator.applyConfig(moduleProject) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("unexpected functional module project ':module'")
        }
    }

    private fun javaProject(path: String) = project(path).also { it.pluginManager.apply("java-library") }

    private fun project(path: String): Project =
        path.split(":").filter { it.isNotEmpty() }.fold(root) { parent, name ->
            parent.childProjects[name] ?: ProjectBuilder.builder().withName(name).withParent(parent).build()
        }

    private fun applicator(vararg modules: Module, configure: ArciphantDsl.() -> Unit = {}) =
        ProjectLayoutConfigApplicator(settings(configure), modules.flatMap { it.toProjectConfigs(PROJECT) })

    private fun settings(configure: ArciphantDsl.() -> Unit = {}) =
        GlobalSettingsRepository(ArciphantDsl().apply(configure)).load()

    private fun domainModule(vararg components: Component, name: String = "module") = DomainModule(
        reference = ModuleReference(name = name),
        components = components.toSet(),
    )

    private fun libraryModule(vararg components: Component, name: String = "library") = LibraryModule(
        reference = ModuleReference(name = name),
        components = components.toSet(),
    )

    private fun bundleModule(vararg includes: ModuleReference, plugin: Plugin? = null) = BundleModule(
        reference = ModuleReference(name = "bundle"),
        plugin = plugin,
        includes = includes.toSet(),
    )
}
