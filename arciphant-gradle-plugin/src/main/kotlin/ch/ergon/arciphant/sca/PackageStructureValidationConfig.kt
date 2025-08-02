package ch.ergon.arciphant.sca

internal data class PackageStructureValidationConfig(
    val basePackagePath: String,
    val useLowerCase: Boolean,
    val removedSpecialCharacters: Set<String>,
    val relativePackagePathsByProjectName: Map<String, String>,
    val absolutePackagePathsByProjectPath: Map<String, String>,
    val excludedProjectPaths: Set<String>,
    val excludedSourceFolders: Set<String>,
)
