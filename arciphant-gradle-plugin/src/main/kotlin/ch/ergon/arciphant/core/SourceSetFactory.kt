package ch.ergon.arciphant.core

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
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

        includeInMainSourceSet(production)
        markAsTestSources(testFixtures, test)

        return ComponentSourceSets(production, testFixtures, test)
    }

    private fun createConsumableConfigurations(sourceSet: SourceSet) {
        val apiElements = createConsumableConfiguration(
            name = sourceSet.apiElementsConfigurationName,
            description = "API elements of the '${sourceSet.name}' source set.",
            superConfigurations = listOf(sourceSet.apiConfiguration()),
        )
        val runtimeElements = createConsumableConfiguration(
            name = sourceSet.runtimeElementsConfigurationName,
            description = "Runtime elements of the '${sourceSet.name}' source set.",
            superConfigurations = runtimeConfigurations(sourceSet),
        )
        listOf(apiElements, runtimeElements).forEach { project.dependencies.add(it.name, sourceSet.output) }
    }

    private fun createConsumableConfiguration(name: String, description: String, superConfigurations: List<Configuration>) =
        project.createConsumableConfiguration(name, description, superConfigurations)

    private fun associate(sourceSet: SourceSet, dependency: SourceSet) {
        val implementation = sourceSet.implementationConfiguration()
        implementation.extendsFrom(dependency.implementationConfiguration())
        project.dependencies.add(implementation.name, dependency.output)
        extendRuntimeOnly(sourceSet, dependency)
        project.associateKotlinCompilations(sourceSet, dependency)
    }

    private fun registerTestTask(testSourceSet: SourceSet) {
        val testTask = if (project.tasks.names.contains(testSourceSet.name)) {
            project.tasks.named(testSourceSet.name, Test::class.java)
        } else {
            project.tasks.register(testSourceSet.name, Test::class.java) {
                description = "Runs the tests of the '${testSourceSet.name}' source set."
                group = LifecycleBasePlugin.VERIFICATION_GROUP
            }
        }
        testTask.configure {
            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath
        }
        if (testSourceSet.name != SourceSet.TEST_SOURCE_SET_NAME) {
            project.tasks.named(SourceSet.TEST_SOURCE_SET_NAME).configure { dependsOn(testTask) }
        }
    }

    private fun includeInMainSourceSet(sourceSet: SourceSet) {
        val mainSourceSet = project.sourceSets().getByName(MAIN_SOURCE_SET_NAME)
        if (sourceSet == mainSourceSet) return

        mainSourceSet.compileClasspath += sourceSet.output
        mainSourceSet.runtimeClasspath += sourceSet.output
        sourceSet.output.classesDirs.forEach {
            mainSourceSet.output.dir(mapOf("builtBy" to sourceSet.classesTaskName), it)
        }
        sourceSet.output.resourcesDir?.let {
            mainSourceSet.output.dir(mapOf("builtBy" to sourceSet.processResourcesTaskName), it)
        }
        mainSourceSet.apiConfiguration().extendsFrom(sourceSet.apiConfiguration())
        mainSourceSet.implementationConfiguration().extendsFrom(sourceSet.implementationConfiguration())
        mainSourceSet.runtimeConfiguration().extendsFrom(sourceSet.runtimeConfiguration())
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
    private fun SourceSet.runtimeConfiguration() = getConfiguration(runtimeOnlyConfigurationName)

    private fun getConfiguration(configurationName: String) = project.getConfiguration(configurationName)
}

internal data class ComponentSourceSets(
    val production: SourceSet,
    val testFixtures: SourceSet?,
    val test: SourceSet?,
)
