package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.ComponentDependencyExtension
import ch.ergon.arciphant.core.GlobalSettings
import ch.ergon.arciphant.core.GlobalSettingsRepository
import ch.ergon.arciphant.core.GradleBundleModuleProjectConfig
import ch.ergon.arciphant.core.GradleFunctionalModuleProjectConfig
import ch.ergon.arciphant.core.GradleProjectPath
import ch.ergon.arciphant.core.model.BundleModule
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.core.model.component
import ch.ergon.arciphant.core.sourceset.CustomizeAllComponentsExtension.Companion.CUSTOMIZE_ALL_COMPONENTS_EXTENSION_NAME
import ch.ergon.arciphant.core.sourceset.CustomizeSingleComponentExtension.Companion.CUSTOMIZE_COMPONENT_EXTENSION_NAME
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.util.projectDependencyConfigurations
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SourceSetLayoutConfigurerTest {

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
    inner class SharedConfigurationsTest {

        @Test
        fun `it should create the shared configurations in functional module projects`() {
            val project = javaProject()

            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings())

            assertThat(project.configurations.names).contains(
                "testFixturesApi",
                "testFixturesImplementation",
                "testFixturesCompileOnly",
                "testFixturesRuntimeOnly",
            )
        }

        @Test
        fun `it should create the shared configurations in bundle module projects`() {
            val project = javaProject(name = "bundle")
            val bundle = BundleModule(
                reference = ModuleReference(name = "bundle"),
                plugin = null,
                includes = emptySet(),
            )

            SourceSetLayoutConfigurer(
                settings(),
                listOf(GradleBundleModuleProjectConfig(GradleProjectPath.of(listOf("bundle")), bundle)),
            ).configure(project)

            assertThat(project.configurations.names)
                .contains("testFixturesApi", "testFixturesImplementation")
        }

        @Test
        fun `it should not create the shared configurations in other projects`() {
            val project = javaProject(name = "not-an-arciphant-module")

            SourceSetLayoutConfigurer(settings(), emptyList()).configure(project)

            assertThat(project.configurations.names).doesNotContain(
                "testFixturesApi",
                "testFixturesImplementation",
                "testFixturesCompileOnly",
                "testFixturesRuntimeOnly",
            )
        }

        @Test
        fun `it should extend the shared configurations for renamed test source sets`() {
            val project = javaProject()
            val settings = settings {
                testSourceSetName { "test-$it" }
                testFixturesSourceSetName { "fixtures-$it" }
            }

            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings)

            val test = project.sourceSets().getByName("test-domain")
            val testFixtures = project.sourceSets().getByName("fixtures-domain")
            assertThat(project.configurations.getByName(test.implementationConfigurationName).extendsFrom)
                .contains(project.configurations.getByName("testImplementation"))
            assertThat(project.configurations.getByName(testFixtures.implementationConfigurationName).extendsFrom)
                .contains(project.configurations.getByName("testFixturesImplementation"))
        }
    }

    @Nested
    inner class DeferredConfigTest {

        @Test
        fun `it should defer the config until a JVM plugin is applied`() {
            val project = ProjectBuilder.builder().withName("module")
                .withParent(ProjectBuilder.builder().withName("root").build()).build()

            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings())

            assertThat(project.extensions.findByType(SourceSetContainer::class.java)).isNull()

            project.pluginManager.apply("java-library")

            assertThat(project.sourceSets().names).contains("domain")
        }
    }

    @Nested
    inner class ComponentDependencyTest {

        @Test
        fun `it should complete component dependencies declared in the dependencies block`() {
            val moduleProject = applyExamAndModuleConfig()

            moduleProject.dependencies.add(
                "domainApi",
                moduleProject.ComponentDependencyFactory(module = "exam", component = "api"),
            )

            assertThat(moduleProject.configurations.getByName("domainRuntimeOnly").projectDependencyConfigurations())
                .containsExactly("apiRuntimeElements")
            assertThat(moduleProject.configurations.getByName("domainTestFixturesApi").projectDependencyConfigurations())
                .containsExactly("apiTestFixturesApiElements")
            assertThat(moduleProject.configurations.getByName("domainTestFixturesRuntimeOnly").projectDependencyConfigurations())
                .containsExactly("apiTestFixturesRuntimeElements")
        }

        @Test
        fun `it should not complete hand-written project dependencies`() {
            val moduleProject = applyExamAndModuleConfig()

            moduleProject.dependencies.add(
                "domainApi",
                moduleProject.dependencies.project(":exam", "apiApiElements"),
            )

            assertThat(moduleProject.configurations.getByName("domainApi").projectDependencyConfigurations())
                .containsExactly("apiApiElements")
            assertThat(moduleProject.configurations.getByName("domainRuntimeOnly").projectDependencyConfigurations())
                .isEmpty()
            assertThat(moduleProject.configurations.getByName("domainTestFixturesApi").projectDependencyConfigurations())
                .isEmpty()
        }

        private fun applyExamAndModuleConfig(): Project {
            val root = ProjectBuilder.builder().withName("root").build()
            val module = domainModule(component(ComponentReference("domain")))
            val exam = DomainModule(
                reference = ModuleReference(name = "exam"),
                components = setOf(component(ComponentReference("api"))),
            )
            val moduleProject = javaProject(root = root)
            val examProject = javaProject(name = "exam", root = root)
            val configurer = SourceSetLayoutConfigurer(
                settings(),
                listOf(
                    GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf("module")), module),
                    GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf("exam")), exam),
                ),
            )
            configurer.configure(moduleProject)
            configurer.configure(examProject)
            return moduleProject
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
            val configurer = SourceSetLayoutConfigurer(
                settings(),
                listOf(
                    GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf("module")), module),
                    GradleBundleModuleProjectConfig(GradleProjectPath.of(listOf("bundle")), bundle),
                ),
            )

            configurer.configure(moduleProject)
            configurer.configure(bundleProject)

            assertThat(bundleProject.configurations.getByName("implementation").projectDependencyConfigurations())
                .containsExactlyInAnyOrder("domainApiElements", "apiApiElements")
            assertThat(bundleProject.configurations.getByName("runtimeOnly").projectDependencyConfigurations())
                .containsExactlyInAnyOrder("domainRuntimeElements", "apiRuntimeElements")
        }

        @Test
        fun `it should not provide the component dependency notation in bundle projects`() {
            val root = ProjectBuilder.builder().withName("root").build()
            val moduleProject = javaProject(root = root)
            val bundleProject = javaProject(name = "bundle", root = root)
            val module = domainModule(component(ComponentReference("domain")))
            val bundle = BundleModule(
                reference = ModuleReference(name = "bundle"),
                plugin = null,
                includes = emptySet(),
            )
            val configurer = SourceSetLayoutConfigurer(
                settings(),
                listOf(
                    GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf("module")), module),
                    GradleBundleModuleProjectConfig(GradleProjectPath.of(listOf("bundle")), bundle),
                ),
            )
            configurer.configure(moduleProject)
            configurer.configure(bundleProject)

            assertThat(bundleProject.extensions.findByType(ComponentDependencyExtension::class.java)).isNull()
            assertThat(moduleProject.extensions.findByType(ComponentDependencyExtension::class.java)).isNotNull()
        }
    }

    @Nested
    inner class ComponentSourceSetCustomizationTest {

        @Test
        fun `it should configure the production source set of every component`() {
            val project = javaProject()
            val module = domainModule(
                component(reference = ComponentReference("domain")),
                component(reference = ComponentReference("api")),
            )
            project.applyModuleConfig(module, settings())

            val configured = mutableListOf<Pair<String, String>>()
            project.customizeAllComponents().productionSourceSet { sourceSet, componentName ->
                configured.add(componentName to sourceSet.name)
            }

            assertThat(configured).containsExactlyInAnyOrder("domain" to "domain", "api" to "api")
        }

        @Test
        fun `it should pass the component name of renamed test source sets`() {
            val project = javaProject()
            val settings = settings {
                testSourceSetName { "test-$it" }
                testFixturesSourceSetName { "fixtures-$it" }
            }
            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings)

            val test = mutableListOf<Pair<String, String>>()
            val testFixtures = mutableListOf<Pair<String, String>>()
            project.customizeAllComponents().testSourceSet { sourceSet, componentName ->
                test.add(componentName to sourceSet.name)
            }
            project.customizeAllComponents().testFixturesSourceSet { sourceSet, componentName ->
                testFixtures.add(componentName to sourceSet.name)
            }

            assertThat(test).containsExactly("domain" to "test-domain")
            assertThat(testFixtures).containsExactly("domain" to "fixtures-domain")
        }

        @Test
        fun `it should skip components without a test or test fixtures source set`() {
            val project = javaProject()
            val module = domainModule(
                component(
                    reference = ComponentReference("domain"),
                    withTestSourceSet = false,
                    withTestFixturesSourceSet = false,
                )
            )
            project.applyModuleConfig(module, settings())

            val configured = mutableListOf<String>()
            project.customizeAllComponents().testSourceSet { _, componentName -> configured.add(componentName) }
            project.customizeAllComponents().testFixturesSourceSet { _, componentName -> configured.add(componentName) }

            assertThat(configured).isEmpty()
        }

        @Test
        fun `it should customize the source directories of all components`() {
            val project = javaProject()
            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings())

            project.customizeAllComponents().productionSourceSet { sourceSet, componentName ->
                sourceSet.java.setSrcDirs(listOf("$componentName/java"))
                sourceSet.resources.setSrcDirs(listOf("$componentName/resources"))
            }

            val production = project.sourceSets().getByName("domain")
            assertThat(production.java.srcDirs).containsExactly(project.projectDir.resolve("domain/java"))
            assertThat(production.resources.srcDirs).containsExactly(project.projectDir.resolve("domain/resources"))
        }

        @Test
        fun `it should configure a single component by name`() {
            val project = javaProject()
            val module = domainModule(
                component(reference = ComponentReference("domain")),
                component(reference = ComponentReference("api")),
            )
            project.applyModuleConfig(module, settings())

            project.customizeComponent()("domain") {
                productionSourceSet { it.java.setSrcDirs(listOf("$componentName/java")) }
                testSourceSet { it.java.setSrcDirs(listOf("$componentName/test/java")) }
                testFixturesSourceSet { it.java.setSrcDirs(listOf("$componentName/testFixtures/java")) }
            }

            assertThat(project.sourceSets().getByName("domain").java.srcDirs)
                .containsExactly(project.projectDir.resolve("domain/java"))
            assertThat(project.sourceSets().getByName("domainTest").java.srcDirs)
                .containsExactly(project.projectDir.resolve("domain/test/java"))
            assertThat(project.sourceSets().getByName("domainTestFixtures").java.srcDirs)
                .containsExactly(project.projectDir.resolve("domain/testFixtures/java"))
            assertThat(project.sourceSets().getByName("api").java.srcDirs)
                .containsExactly(project.projectDir.resolve("src/api/java"))
        }

        @Test
        fun `it should fail for an unknown component name`() {
            val project = javaProject()
            project.applyModuleConfig(domainModule(component(ComponentReference("domain"))), settings())

            assertThatThrownBy {
                project.customizeComponent()("unknown") { }
            }.hasMessageContaining("unknown component 'unknown'")
                .hasMessageContaining("'domain'")
        }

        @Test
        fun `it should fail when a single component has no requested source set`() {
            val project = javaProject()
            val module = domainModule(
                component(
                    reference = ComponentReference("domain"),
                    withTestSourceSet = false,
                    withTestFixturesSourceSet = false,
                )
            )
            project.applyModuleConfig(module, settings())

            assertThatThrownBy {
                project.customizeComponent()("domain") { testSourceSet { } }
            }.hasMessageContaining("component 'domain' has no test source set")
            assertThatThrownBy {
                project.customizeComponent()("domain") { testFixturesSourceSet { } }
            }.hasMessageContaining("component 'domain' has no test fixtures source set")
        }

        @Test
        fun `it should not register the extensions in bundle module projects`() {
            val project = javaProject(name = "bundle")
            val bundle = BundleModule(
                reference = ModuleReference(name = "bundle"),
                plugin = null,
                includes = emptySet(),
            )
            SourceSetLayoutConfigurer(
                settings(),
                listOf(GradleBundleModuleProjectConfig(GradleProjectPath.of(listOf("bundle")), bundle)),
            ).configure(project)

            assertThat(project.extensions.findByName(CUSTOMIZE_ALL_COMPONENTS_EXTENSION_NAME)).isNull()
            assertThat(project.extensions.findByName(CUSTOMIZE_COMPONENT_EXTENSION_NAME)).isNull()
        }

        private fun Project.customizeAllComponents() =
            extensions.getByName(CUSTOMIZE_ALL_COMPONENTS_EXTENSION_NAME) as CustomizeAllComponentsExtension

        private fun Project.customizeComponent() =
            extensions.getByName(CUSTOMIZE_COMPONENT_EXTENSION_NAME) as CustomizeSingleComponentExtension
    }

    private fun javaProject(
        name: String = "module",
        root: Project = ProjectBuilder.builder().withName("root").build(),
    ): Project {
        return ProjectBuilder.builder().withName(name).withParent(root).build()
            .also { it.pluginManager.apply("java-library") }
    }

    private fun Project.ComponentDependencyFactory(module: String, component: String) =
        extensions.getByType(ComponentDependencyExtension::class.java).invoke(module = module, component = component)

    private fun settings(configure: ArciphantDsl.() -> Unit = {}) = GlobalSettingsRepository(
        ArciphantDsl().apply {
            sourceSetComponentLayout()
            configure()
        }
    ).load()

    private fun domainModule(vararg components: Component) = DomainModule(
        reference = ModuleReference(name = "module"),
        components = components.toSet(),
    )

    private fun Project.applyModuleConfig(module: FunctionalModule, settings: GlobalSettings) =
        SourceSetLayoutConfigurer(
            settings,
            listOf(GradleFunctionalModuleProjectConfig(GradleProjectPath.of(listOf(name)), module)),
        ).configure(this)

}
