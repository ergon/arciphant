package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.sca.GlobalPackageStructureValidationSettings
import ch.ergon.arciphant.sca.packageToFolderPath
import ch.ergon.arciphant.util.verifyName

internal class PackageStructureValidationBuilder : PackageStructureValidationDsl {

    private var basePackageName: String? = null
    private var useLowerCase: Boolean = true
    private var removeUnderscore: Boolean = true
    private var removeHyphen: Boolean = true

    private val excludedProjectPaths = mutableSetOf<String>()

    private var excludeResourcesFolder: Boolean = false
    private val excludedSrcFolders = mutableSetOf<String>()

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

    override fun excludeProjectPath(projectPath: String) {
        this.excludedProjectPaths.add(projectPath)
    }

    override fun excludeResourcesFolder() {
        this.excludeResourcesFolder = true
    }

    override fun excludeSrcFolders(folderName: String) {
        this.excludedSrcFolders.add(folderName)
    }

    fun build(): GlobalPackageStructureValidationSettings {
        basePackageName?.let { verifyName(it, "base package name", forbidEmpty = true) }
        excludedSrcFolders.forEach { verifyName(it, "source folder name", forbidEmpty = true) }
        return GlobalPackageStructureValidationSettings(
            basePackagePath = basePackageName?.packageToFolderPath(),
            useLowerCase = useLowerCase,
            removedSpecialCharacters = setOfNotNull(
                if (removeUnderscore) "_" else null,
                if (removeHyphen) "-" else null,
            ),
            excludedProjectPaths = excludedProjectPaths,
            excludedSourceFolders = excludedSrcFolders + listOfNotNull(if(excludeResourcesFolder) "*/resources" else null)
        )
    }

}
