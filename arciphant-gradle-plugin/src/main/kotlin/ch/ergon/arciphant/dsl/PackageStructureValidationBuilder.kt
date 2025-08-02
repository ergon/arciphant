package ch.ergon.arciphant.dsl

internal class PackageStructureValidationBuilder : PackageStructureValidationDsl {

    var basePackageName: String = ""
    var useLowerCase: Boolean = true
    var removeUnderscore: Boolean = true
    var removeHyphen: Boolean = true

    val relativePackagesByProjectName = mutableMapOf<String, String>()
    val absolutePackagesByProjectPath = mutableMapOf<String, String>()
    val excludedProjectPaths = mutableSetOf<String>()

    var excludeResourcesFolder: Boolean = false
    val excludedSrcFolders = mutableSetOf<String>()

    override fun basePackageName(basePackageName: String) {
        this.basePackageName = basePackageName
    }

    override fun disableUseLowerCase() {
        this.useLowerCase = false
    }

    override fun disableRemoveUnderscore() {
        this.removeUnderscore = false
    }

    override fun disableRemoveHyphen() {
        this.removeHyphen = false
    }

    override fun mapProjectNamesToPackageFragments(vararg projectNameToPackageFragment: Pair<String, String>) {
        this.relativePackagesByProjectName.putAll(projectNameToPackageFragment)
    }

    override fun mapProjectPathsToAbsolutePackages(vararg projectPathToAbsolutePackage: Pair<String, String>) {
        this.absolutePackagesByProjectPath.putAll(projectPathToAbsolutePackage)
    }

    override fun excludeProjectPath(projectPath: String) {
        this.excludedProjectPaths.add(projectPath)
    }

    override fun excludeResourcesFolder() {
        this.excludeResourcesFolder = true
    }

    override fun excludeSrcFolders(folderName: String) {
        this.excludedSrcFolders.add(folderName)
    }

}
