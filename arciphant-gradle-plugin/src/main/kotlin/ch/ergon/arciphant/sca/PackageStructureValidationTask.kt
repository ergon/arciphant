package ch.ergon.arciphant.sca

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

internal fun Project.registerValidatePackageStructureTask(validator: PackageStructureValidator) {
    tasks.register("validatePackageStructure") {
        doLast {
            validatePackageStructure(validator, logger)
        }
    }
}

private fun Project.validatePackageStructure(validator: PackageStructureValidator, logger: Logger) {
    project.allprojects.forEach { currentProject ->
        when(val result = validator.validate(currentProject)) {
            is ValidPackageStructure -> {
                logger.debug("Package structure of project '${currentProject.path}' is valid")
            }
            is InvalidPackageStructure -> {
                result.invalidFiles.forEach {
                    logger.error("Source file '${it.path}' has invalid package name.")
                }
                throw GradleException("There are source files with invalid package names. See error log above.")
            }
        }
    }
}
