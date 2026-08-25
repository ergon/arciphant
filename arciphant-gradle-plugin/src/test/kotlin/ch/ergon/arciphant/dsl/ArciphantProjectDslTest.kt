package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.sourceSetComponentSettings
import ch.ergon.arciphant.core.sourceset.SourceSetFactory
import ch.ergon.arciphant.util.configuration
import ch.ergon.arciphant.util.javaProject
import ch.ergon.arciphant.util.projectDependencyConfigurations
import ch.ergon.arciphant.util.projectDependencyPaths
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class ArciphantProjectDslTest {

    private val root = ProjectBuilder.builder().withName("root").build()
    private val project = javaProject(name = "certificate", parent = root)
    private val defaultSettings = sourceSetComponentSettings()

    init {
        javaProject(name = "exam", parent = root)
    }

    @Test
    fun `it should add an api dependency on a component of another module`() {
        val dsl = dsl()
        createComponent(name = "domain")

        with(dsl) { component("domain").api(module = "exam", component = "api") }

        assertThat(project.configuration("domainApi").projectDependencyPaths()).containsExactly(":exam")
        assertThat(project.configuration("domainApi").projectDependencyConfigurations())
            .containsExactly("apiApiElements")
        assertThat(project.configuration("domainRuntimeOnly").projectDependencyConfigurations())
            .containsExactly("apiRuntimeElements")
    }

    @Test
    fun `it should add an implementation dependency on a component of another module`() {
        val dsl = dsl()
        createComponent(name = "domain")

        with(dsl) { component("domain").implementation(module = "exam", component = "api") }

        assertThat(project.configuration("domainImplementation").projectDependencyConfigurations())
            .containsExactly("apiApiElements")
        assertThat(project.configuration("domainApi").projectDependencyConfigurations()).isEmpty()
    }

    @Test
    fun `it should link test fixtures when both source and target component have a test fixtures source set`() {
        val dsl = dsl()
        createComponent(name = "domain")

        with(dsl) { component("domain").api(module = "exam", component = "api") }

        assertThat(project.configuration("domainTestFixturesApi").projectDependencyPaths())
            .containsExactly(":exam")
        assertThat(project.configuration("domainTestFixturesApi").projectDependencyConfigurations())
            .containsExactly("apiTestFixturesApiElements")
    }

    @Test
    fun `it should not link test fixtures when the target component has no test fixtures source set`() {
        val targetComponent = component(ComponentReference("api"), withTestFixturesSourceSet = false)
        val dsl = dsl(modules = listOf(examModule(targetComponent)))
        createComponent(name = "domain")

        with(dsl) { component("domain").api(module = "exam", component = "api") }

        assertThat(project.configuration("domainApi").projectDependencyConfigurations())
            .containsExactly("apiApiElements")
        assertThat(project.configuration("domainTestFixturesApi").projectDependencyConfigurations()).isEmpty()
    }

    @Test
    fun `it should not link test fixtures when the source component has no test fixtures source set`() {
        val dsl = dsl()
        createComponent(name = "domain", withTestFixturesSourceSet = false)

        with(dsl) { component("domain").api(module = "exam", component = "api") }

        assertThat(project.configuration("domainApi").projectDependencyConfigurations())
            .containsExactly("apiApiElements")
    }

    @Test
    fun `it should fall back to the global test fixtures setting for the target component`() {
        val settings = sourceSetComponentSettings(withTestFixturesSourceSet = false)
        val dsl = dsl(settings = settings)
        createComponent(name = "domain", settings = settings, withTestFixturesSourceSet = true)

        with(dsl) { component("domain").api(module = "exam", component = "api") }

        assertThat(project.configuration("domainTestFixturesApi").projectDependencyConfigurations()).isEmpty()
    }

    @Test
    fun `it should reject an unknown target module`() {
        val dsl = dsl()
        createComponent(name = "domain")

        assertThatThrownBy { with(dsl) { component("domain").api(module = "billing", component = "api") } }
            .hasMessage("Arciphant configuration error: Module with name 'billing' does not exist.")
    }

    @Test
    fun `it should reject an unknown component of the target module`() {
        val dsl = dsl()
        createComponent(name = "domain")

        assertThatThrownBy { with(dsl) { component("domain").api(module = "exam", component = "db") } }
            .hasMessage("Arciphant configuration error: Component with name 'db' does not exist in module 'exam'.")
    }

    @Test
    fun `it should reject an unknown source component`() {
        val dsl = dsl()

        assertThatThrownBy { with(dsl) { component("domain").api(module = "exam", component = "api") } }
            .hasMessage("Arciphant configuration error: Component with name 'domain' does not exist in project ':certificate'.")
    }

    private fun dsl(
        modules: List<Module> = listOf(examModule(component(ComponentReference("api")))),
        settings: SourceSetComponentSettings = defaultSettings,
    ) = ArciphantProjectDsl(project, modules, SOURCE_SET, settings)

    private fun createComponent(
        name: String,
        settings: SourceSetComponentSettings = defaultSettings,
        withTestFixturesSourceSet: Boolean? = null,
    ) = SourceSetFactory(project).createComponent(
        name = name,
        settings = settings,
        withTestFixturesSourceSet = withTestFixturesSourceSet,
    )

    private fun examModule(vararg components: Component) = DomainModule(
        reference = ModuleReference(name = "exam"),
        components = components.toSet(),
    )

}
