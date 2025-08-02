package ch.ergon.arciphant.sca

import org.gradle.api.GradleException
import org.gradle.api.Project

internal fun Project.registerValidatePackageStructureTask(validator: PackageStructureValidator) {
    tasks.register("validatePackageStructure") {
        doLast {
            validatePackageStructure(validator)
        }
    }
}

private fun Project.validatePackageStructure(validator: PackageStructureValidator) {
    var hasError = false

    project.allprojects.forEach { currentProject ->
        logger.info("Validate package structure of project '${currentProject.path}'.")
        when (val result = validator.validate(currentProject)) {
            is ValidPackageStructure -> {
                logger.info("Package structure of project '${currentProject.path}' is valid.")
            }

            is InvalidPackageStructure -> {
                hasError = true
                result.invalidFiles.forEach {
                    logger.error("Source file '${it.path}' has invalid package name.")
                }
            }
        }
    }

    if (hasError) {
        throw GradleException("There are source files with invalid package names. See error log above.")
    }
}
