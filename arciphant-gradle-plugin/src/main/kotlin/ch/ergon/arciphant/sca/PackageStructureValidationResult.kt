package ch.ergon.arciphant.sca

import java.io.File

sealed interface PackageStructureValidationResult

object ValidPackageStructure : PackageStructureValidationResult

data class InvalidPackageStructure(val invalidFiles: Set<File>) : PackageStructureValidationResult
