package ch.ergon.arciphant.core

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

internal class SourceSetFactory(private val project: Project) {

    fun createComponent(
        name: String,
        settings: SourceSetComponentSettings,
        withTestSourceSet: Boolean? = null,
        withTestFixturesSourceSet: Boolean? = null,
        sourceSetDependenciesBlock: (SourceSetDependencyScope.(SourceSet) -> Unit)? = null,
    ): ComponentSourceSets {
        val sourceSets = project.sourceSets()
        val production = sourceSets.create(name)
        createOrReuseApiConfiguration(production)

        val testFixtures = if (withTestFixturesSourceSet ?: settings.withTestFixturesSourceSet) {
            sourceSets.create(settings.testFixturesSourceSetName(name)).also {
                createOrReuseApiConfiguration(it)
                associate(it, production)
            }
        } else {
            null
        }

        val test = if (withTestSourceSet ?: settings.withTestSourceSet) {
            sourceSets.create(settings.testSourceSetName(name)).also {
                associate(it, testFixtures ?: production)
                registerTestTask(it)
            }
        } else {
            null
        }

        createConsumableConfigurations(production)
        testFixtures?.let { createConsumableConfigurations(it) }

        sourceSetDependenciesBlock?.let { block -> project.sourceSetDependencies(settings) { block(production) } }

        attachToLifecycleTasks(production, testFixtures, test)
        markAsTestSources(testFixtures, test)

        return ComponentSourceSets(production, testFixtures, test)
    }

    private fun createConsumableConfigurations(sourceSet: SourceSet) {
        val apiElements = createConsumableConfiguration(
            name = sourceSet.name.apiElementsConfigurationName(),
            description = "API elements of the '${sourceSet.name}' source set.",
            superConfigurations = listOf(sourceSet.apiConfiguration()),
        )
        val runtimeElements = createConsumableConfiguration(
            name = sourceSet.name.runtimeElementsConfigurationName(),
            description = "Runtime elements of the '${sourceSet.name}' source set.",
            superConfigurations = runtimeConfigurations(sourceSet),
        )
        listOf(apiElements, runtimeElements).forEach { project.dependencies.add(it.name, sourceSet.output) }
    }

    private fun createConsumableConfiguration(name: String, description: String, superConfigurations: List<Configuration>): Configuration {
        return project.configurations.create(name) {
            isCanBeConsumed = true
            isCanBeResolved = false
            this.description = description
            extendsFrom(*superConfigurations.toTypedArray())
        }
    }

    private fun associate(sourceSet: SourceSet, dependency: SourceSet) {
        val implementation = sourceSet.implementationConfiguration()
        implementation.extendsFrom(dependency.implementationConfiguration())
        project.dependencies.add(implementation.name, dependency.output)
        extendRuntimeOnly(sourceSet, dependency)
        project.associateKotlinCompilations(sourceSet, dependency)
    }

    private fun registerTestTask(testSourceSet: SourceSet) {
        val testTask = project.tasks.register(testSourceSet.name, Test::class.java) {
            description = "Runs the tests of the '${testSourceSet.name}' source set."
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath
        }
        project.tasks.named("test").configure { dependsOn(testTask) }
    }

    private fun attachToLifecycleTasks(production: SourceSet, testFixtures: SourceSet?, test: SourceSet?) {
        attachToLifecycleTask(JavaPlugin.CLASSES_TASK_NAME, production)
        listOfNotNull(testFixtures, test).forEach { attachToLifecycleTask(JavaPlugin.TEST_CLASSES_TASK_NAME, it) }
    }

    private fun attachToLifecycleTask(lifecycleTaskName: String, sourceSet: SourceSet) {
        project.tasks.named(lifecycleTaskName).configure { dependsOn(sourceSet.classesTaskName) }
    }

    private fun markAsTestSources(vararg sourceSets: SourceSet?) {
        project.pluginManager.withPlugin("idea") {
            val testSources = project.extensions.getByType(IdeaModel::class.java).module.testSources
            sourceSets.filterNotNull().forEach { testSources.from(it.allSource.srcDirs) }
        }
    }

    private fun createOrReuseApiConfiguration(sourceSet: SourceSet): Configuration {
        return project.configurations.maybeCreate(sourceSet.apiConfigurationName).apply {
            isCanBeConsumed = false
            isCanBeResolved = false
            isVisible = false
            description = "API dependencies for the '${sourceSet.name}' source set."
            sourceSet.implementationConfiguration().extendsFrom(this)
        }
    }

    private fun runtimeConfigurations(sourceSet: SourceSet): List<Configuration> = project.runtimeConfigurations(sourceSet)

    private fun extendRuntimeOnly(sourceSet: SourceSet, dependency: SourceSet) = project.extendRuntimeOnly(sourceSet, dependency)

    private fun SourceSet.apiConfiguration() = getConfiguration(apiConfigurationName)
    private fun SourceSet.implementationConfiguration() = getConfiguration(implementationConfigurationName)

    private fun getConfiguration(configurationName: String) = project.getConfiguration(configurationName)
}

internal data class ComponentSourceSets(
    val production: SourceSet,
    val testFixtures: SourceSet?,
    val test: SourceSet?,
)
