package ch.ergon.arciphant.sca

import ch.ergon.arciphant.dsl.ArciphantProjectDsl
import ch.ergon.arciphant.util.packageToFolderPath
import ch.ergon.arciphant.util.verify
import ch.ergon.arciphant.util.verifyName
import org.gradle.api.Project

internal fun Project.arciphantProjectDsl(): ArciphantProjectDsl? =
    extensions.findByType(ArciphantProjectDsl::class.java)

internal fun ArciphantProjectDsl.verify(projectPath: String) {
    verify(packageName == null || absolutePackageName == null) {
        "project '$projectPath' must not configure both 'packageName' and 'absolutePackageName'"
    }
    packageName?.let { verifyName(it, "package") }
    absolutePackageName?.let { verifyName(it, "absolute package", forbidEmpty = true) }
}

internal fun ArciphantProjectDsl.relativePackagePath(): String? = packageName?.packageToFolderPath()

internal fun ArciphantProjectDsl.absolutePackagePath(): String? = absolutePackageName?.packageToFolderPath()
