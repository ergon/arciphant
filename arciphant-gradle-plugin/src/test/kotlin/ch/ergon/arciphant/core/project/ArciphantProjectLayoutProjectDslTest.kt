package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.core.model.component
import ch.ergon.arciphant.util.configuration
import ch.ergon.arciphant.util.javaProject
import ch.ergon.arciphant.util.projectDependencyPaths
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class ArciphantProjectLayoutProjectDslTest {

    private val root = ProjectBuilder.builder().withName("root").build()
    private val project = javaProject(name = "domain", parent = root)

    init {
        val exam = subProject(name = "exam", parent = root)
        subProject(name = "api", parent = exam)
    }

    @Test
    fun `it should add an api dependency on a component project of another module`() {
        dsl().api(module = "exam", component = "api")

        assertThat(project.configuration("api").projectDependencyPaths()).containsExactly(":exam:api")
        assertThat(project.configuration("implementation").projectDependencyPaths()).isEmpty()
    }

    @Test
    fun `it should add an implementation dependency on a component project of another module`() {
        dsl().implementation(module = "exam", component = "api")

        assertThat(project.configuration("implementation").projectDependencyPaths()).containsExactly(":exam:api")
        assertThat(project.configuration("api").projectDependencyPaths()).isEmpty()
    }

    @Test
    fun `it should resolve the target project path from the module reference path`() {
        val modulesFolder = subProject(name = "modules", parent = root)
        val course = subProject(name = "course", parent = modulesFolder)
        subProject(name = "api", parent = course)
        val module = DomainModule(
            reference = ModuleReference(parentProjectPath = listOf("modules"), name = "course"),
            components = setOf(component(ComponentReference("api"))),
        )

        dsl(modules = listOf(module)).api(module = "course", component = "api")

        assertThat(project.configuration("api").projectDependencyPaths()).containsExactly(":modules:course:api")
    }

    @Test
    fun `it should add a test fixtures dependency when the java-test-fixtures plugin is applied`() {
        project.pluginManager.apply("java-test-fixtures")

        dsl().api(module = "exam", component = "api")

        // besides the self-dependency added by the java-test-fixtures plugin itself
        assertThat(project.configuration("testFixturesApi").projectDependencyPaths()).contains(":exam:api")
    }

    @Test
    fun `it should defer the test fixtures dependency until the java-test-fixtures plugin is applied`() {
        dsl().api(module = "exam", component = "api")

        assertThat(project.configurations.findByName("testFixturesApi")).isNull()

        project.pluginManager.apply("java-test-fixtures")

        assertThat(project.configuration("testFixturesApi").projectDependencyPaths()).contains(":exam:api")
    }

    @Test
    fun `it should reject an unknown target module`() {
        assertThatThrownBy { dsl().api(module = "billing", component = "api") }
            .hasMessage("Arciphant configuration error: Module with name 'billing' does not exist.")
    }

    @Test
    fun `it should require a JVM plugin`() {
        val projectWithoutJvmPlugin = subProject(name = "web", parent = root)
        val dsl = ArciphantProjectLayoutProjectDsl(projectWithoutJvmPlugin, examModules())

        assertThatThrownBy { dsl.api(module = "exam", component = "api") }
            .hasMessageStartingWith("Arciphant error: configuration 'api' does not exist")
    }

    private fun dsl(modules: List<Module> = examModules()) = ArciphantProjectLayoutProjectDsl(project, modules)

    private fun examModules(): List<Module> = listOf(
        DomainModule(
            reference = ModuleReference(name = "exam"),
            components = setOf(component(ComponentReference("api"))),
        )
    )

    private fun subProject(name: String, parent: Project): Project =
        ProjectBuilder.builder().withName(name).withParent(parent).build()

}
