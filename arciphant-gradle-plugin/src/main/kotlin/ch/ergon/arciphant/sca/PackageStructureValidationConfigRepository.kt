package ch.ergon.arciphant.sca

import ch.ergon.arciphant.dsl.PackageStructureValidationBuilder

internal class PackageStructureValidationConfigRepository(private val builder: PackageStructureValidationBuilder) {

    fun load(): PackageStructureValidationConfig {
        return PackageStructureValidationConfig(
            basePackageName = builder.basePackageName,
            useLowerCase = builder.useLowerCase,
            removedSpecialCharacters = setOfNotNull(
                if (builder.removeUnderscore) "_" else null,
                if (builder.removeHyphen) "-" else null,
            ),
            relativePackagesByProjectName = builder.relativePackagesByProjectName,
            absolutePackagesByProjectPath = builder.absolutePackagesByProjectPath,
            excludedProjectPaths = builder.excludedProjectPaths,
            excludedSourceFolders = builder.excludedSrcFolders + listOfNotNull(
                if (builder.excludeResourcesFolder) "main/resources" else null,
            )
        )
    }
}
