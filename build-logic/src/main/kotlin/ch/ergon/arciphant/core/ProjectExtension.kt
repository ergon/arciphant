package ch.ergon.arciphant.core

import org.gradle.api.Project

internal fun Project.childProject(reference: NamedReference) = childProjects.getValue(reference.name)
