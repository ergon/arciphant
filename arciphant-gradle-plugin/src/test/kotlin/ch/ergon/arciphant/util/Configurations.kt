package ch.ergon.arciphant.util

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSet

fun Configuration.projectDependencyConfigurations() =
    dependencies.withType(ProjectDependency::class.java).map { it.targetConfiguration }

fun Configuration.projectDependencyPaths() =
    dependencies.withType(ProjectDependency::class.java).map { it.path }

fun Configuration.hasFileDependencyOn(sourceSet: SourceSet) =
    dependencies.withType(FileCollectionDependency::class.java)
        .any { it.files.files.containsAll(sourceSet.output.files) }