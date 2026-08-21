package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

internal data class ComponentSourceSets(
    val production: SourceSet,
    val testFixtures: SourceSet?,
    val test: SourceSet?,
)

internal fun Project.createComponent(
    name: String,
    settings: SourceSetComponentSettings,
    withTestSourceSet: Boolean? = null,
    withTestFixturesSourceSet: Boolean? = null,
    consumable: Boolean = false,
    sourceSetDependenciesBlock: (SourceSetDependencyScope.(SourceSet) -> Unit)? = null,
): ComponentSourceSets {
    val sourceSets = sourceSets()
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

    if (consumable) {
        createConsumableConfigurations(production)
        testFixtures?.let { createConsumableConfigurations(it) }
    }

    sourceSetDependenciesBlock?.let { block -> sourceSetDependencies(settings) { block(production) } }

    includeInMainSourceSet(production)
    markAsTestSources(testFixtures, test)

    return ComponentSourceSets(production, testFixtures, test)
}

internal fun Project.sourceSetDependencies(
    settings: SourceSetComponentSettings,
    block: SourceSetDependencyScope.() -> Unit,
) {
    SourceSetDependencyScope(this, settings).block()
}

internal fun Project.createConsumableModuleConfigurations(componentSourceSets: Collection<ComponentSourceSets>) {
    val apiElements = createConsumableConfiguration(
        name = MODULE_API_ELEMENTS_CONFIGURATION,
        description = "API elements of all Arciphant component source sets in this module.",
        superConfigurations = componentSourceSets.map { apiConfiguration(it.production) },
    )
    val runtimeElements = createConsumableConfiguration(
        name = MODULE_RUNTIME_ELEMENTS_CONFIGURATION,
        description = "Runtime elements of all Arciphant component source sets in this module.",
        superConfigurations = componentSourceSets.flatMap { runtimeConfigurations(it.production) },
    )
    listOf(apiElements, runtimeElements).forEach { artifacts.add(it.name, tasks.named("jar")) }
}

class SourceSetDependencyScope internal constructor(
    private val project: Project,
    private val settings: SourceSetComponentSettings,
) {

    fun implementation(sourceSet: SourceSet, dependency: SourceSet) {
        addLocalDependency(IMPLEMENTATION, sourceSet, dependency)
    }

    fun api(sourceSet: SourceSet, dependency: SourceSet) {
        addLocalDependency(API, sourceSet, dependency)
    }

    fun implementation(
        sourceSet: SourceSet,
        projectPath: String,
        componentName: String,
        withTestFixturesSourceSet: Boolean? = null,
    ) {
        addProjectDependency(IMPLEMENTATION, sourceSet, projectPath, componentName, withTestFixturesSourceSet)
    }

    fun api(
        sourceSet: SourceSet,
        projectPath: String,
        componentName: String,
        withTestFixturesSourceSet: Boolean? = null,
    ) {
        addProjectDependency(API, sourceSet, projectPath, componentName, withTestFixturesSourceSet)
    }

    internal fun addLocalDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        addDependency(type, sourceSet, dependency)

        val sourceTestFixtures = sourceSet.testFixturesSourceSet()
        val dependencyTestFixtures = dependency.testFixturesSourceSet()
        if (sourceTestFixtures != null && dependencyTestFixtures != null) {
            addDependency(type, sourceTestFixtures, dependencyTestFixtures)
        }
    }

    internal fun addProjectDependency(
        type: DependencyType,
        sourceSet: SourceSet,
        projectPath: String,
        componentName: String,
        withTestFixturesSourceSet: Boolean? = null,
    ) {
        addDependency(type, sourceSet, projectPath, componentName)

        val sourceTestFixtures = sourceSet.testFixturesSourceSet()
        if (sourceTestFixtures != null && (withTestFixturesSourceSet ?: settings.withTestFixturesSourceSet)) {
            addDependency(type, sourceTestFixtures, projectPath, settings.testFixturesSourceSetName(componentName))
        }
    }

    private fun addDependency(type: DependencyType, sourceSet: SourceSet, dependency: SourceSet) {
        val dependencyConfiguration = project.dependencyConfiguration(sourceSet, type)
        dependencyConfiguration.extendsFrom(project.apiConfiguration(dependency))
        project.dependencies.add(dependencyConfiguration.name, dependency.output)
        project.extendRuntimeOnly(sourceSet, dependency)
    }

    private fun addDependency(
        type: DependencyType,
        sourceSet: SourceSet,
        projectPath: String,
        dependencySourceSetName: String,
    ) {
        project.dependencies.add(
            project.dependencyConfiguration(sourceSet, type).name,
            project.createProjectDependency(projectPath, dependencySourceSetName.apiElementsConfigurationName()),
        )
        project.dependencies.add(
            sourceSet.runtimeOnlyConfigurationName,
            project.createProjectDependency(projectPath, dependencySourceSetName.runtimeElementsConfigurationName()),
        )
    }

    private fun SourceSet.testFixturesSourceSet(): SourceSet? =
        project.sourceSets().findByName(settings.testFixturesSourceSetName(name))
}

private fun Project.createConsumableConfigurations(sourceSet: SourceSet) {
    val apiElements = createConsumableConfiguration(
        name = sourceSet.name.apiElementsConfigurationName(),
        description = "API elements of the '${sourceSet.name}' source set.",
        superConfigurations = listOf(apiConfiguration(sourceSet)),
    )
    val runtimeElements = createConsumableConfiguration(
        name = sourceSet.name.runtimeElementsConfigurationName(),
        description = "Runtime elements of the '${sourceSet.name}' source set.",
        superConfigurations = runtimeConfigurations(sourceSet),
    )
    listOf(apiElements, runtimeElements).forEach { dependencies.add(it.name, sourceSet.output) }
}

private fun Project.createConsumableConfiguration(
    name: String,
    description: String,
    superConfigurations: List<Configuration>,
): Configuration = configurations.create(name) {
    isCanBeConsumed = true
    isCanBeResolved = false
    this.description = description
    extendsFrom(*superConfigurations.toTypedArray())
}

private fun Project.associate(sourceSet: SourceSet, dependency: SourceSet) {
    val implementation = implementationConfiguration(sourceSet)
    implementation.extendsFrom(implementationConfiguration(dependency))
    dependencies.add(implementation.name, dependency.output)
    extendRuntimeOnly(sourceSet, dependency)
    associateKotlinCompilations(sourceSet, dependency)
}

private fun Project.registerTestTask(testSourceSet: SourceSet) {
    val testTask = if (tasks.names.contains(testSourceSet.name)) {
        tasks.named(testSourceSet.name, Test::class.java)
    } else {
        tasks.register(testSourceSet.name, Test::class.java) {
            description = "Runs the tests of the '${testSourceSet.name}' source set."
            group = LifecycleBasePlugin.VERIFICATION_GROUP
        }
    }
    testTask.configure {
        testClassesDirs = testSourceSet.output.classesDirs
        classpath = testSourceSet.runtimeClasspath
    }
    if (testSourceSet.name != SourceSet.TEST_SOURCE_SET_NAME) {
        tasks.named(SourceSet.TEST_SOURCE_SET_NAME).configure { dependsOn(testTask) }
    }
}

private fun Project.includeInMainSourceSet(sourceSet: SourceSet) {
    val mainSourceSet = sourceSets().getByName(MAIN_SOURCE_SET_NAME)
    if (sourceSet == mainSourceSet) return

    mainSourceSet.compileClasspath += sourceSet.output
    mainSourceSet.runtimeClasspath += sourceSet.output
    sourceSet.output.classesDirs.forEach {
        mainSourceSet.output.dir(mapOf("builtBy" to sourceSet.classesTaskName), it)
    }
    sourceSet.output.resourcesDir?.let {
        mainSourceSet.output.dir(mapOf("builtBy" to sourceSet.processResourcesTaskName), it)
    }
    apiConfiguration(mainSourceSet).extendsFrom(apiConfiguration(sourceSet))
    implementationConfiguration(mainSourceSet).extendsFrom(implementationConfiguration(sourceSet))
    runtimeConfiguration(mainSourceSet).extendsFrom(runtimeConfiguration(sourceSet))
}

private fun Project.markAsTestSources(vararg sourceSets: SourceSet?) {
    pluginManager.withPlugin("idea") {
        val testSources = extensions.getByType(IdeaModel::class.java).module.testSources
        sourceSets.filterNotNull().forEach { testSources.from(it.allSource.srcDirs) }
    }
}

private fun Project.dependencyConfiguration(sourceSet: SourceSet, type: DependencyType) = when (type) {
    API -> apiConfiguration(sourceSet)
    IMPLEMENTATION -> implementationConfiguration(sourceSet)
}

private fun Project.apiConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.apiConfigurationName)
private fun Project.implementationConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.implementationConfigurationName)
private fun Project.runtimeConfiguration(sourceSet: SourceSet) = getConfiguration(sourceSet.runtimeOnlyConfigurationName)

private fun Project.createOrReuseApiConfiguration(sourceSet: SourceSet): Configuration {
    return configurations.maybeCreate(sourceSet.apiConfigurationName).apply {
        isCanBeConsumed = false
        isCanBeResolved = false
        isVisible = false
        description = "API dependencies for the '${sourceSet.name}' source set."
        implementationConfiguration(sourceSet).extendsFrom(this)
    }
}

private fun Project.runtimeConfigurations(sourceSet: SourceSet): List<Configuration> = listOf(
    implementationConfiguration(sourceSet),
    runtimeConfiguration(sourceSet),
)

private fun Project.extendRuntimeOnly(sourceSet: SourceSet, dependency: SourceSet) {
    runtimeConfiguration(sourceSet)
        .extendsFrom(*runtimeConfigurations(dependency).toTypedArray())
}

private fun Project.createProjectDependency(projectPath: String, targetConfiguration: String): Dependency =
    dependencies.project(
        mapOf(
            "path" to projectPath,
            "configuration" to targetConfiguration,
        )
    )

internal fun Project.sourceSets(): SourceSetContainer =
    extensions.findByType(SourceSetContainer::class.java)
        ?: throw IllegalArgumentException("Arciphant error: cannot access source sets in project '$path' because no compatible JVM plugin has been applied.")

internal fun Project.getConfiguration(configurationName: String): Configuration =
    configurations.getByName(configurationName)

internal fun String.defaultTestSourceSetName() = "${this}Test"
internal fun String.defaultTestFixturesSourceSetName() = "${this}TestFixtures"

private fun String.apiElementsConfigurationName() = "${this}ApiElements"
private fun String.runtimeElementsConfigurationName() = "${this}RuntimeElements"

internal const val MODULE_API_ELEMENTS_CONFIGURATION = "arciphantModuleApiElements"
internal const val MODULE_RUNTIME_ELEMENTS_CONFIGURATION = "arciphantModuleRuntimeElements"
