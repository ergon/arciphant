package ch.ergon.arciphant.sca

internal data class PackageStructureValidationConfig(
    val basePackageName: String,
    val useLowerCase: Boolean,
    val removedSpecialCharacters: Set<String>,
    val relativePackagesByProjectName: Map<String, String>,
    val absolutePackagesByProjectPath: Map<String, String>,
    val excludedProjectPaths: Set<String>,
    val excludedSourceFolders: Set<String>,
)
