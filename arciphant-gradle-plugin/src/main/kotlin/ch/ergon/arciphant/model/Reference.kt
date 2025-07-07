package ch.ergon.arciphant.model

sealed interface ModuleReference {
    val name: String
}

sealed class FunctionalModuleReference : ModuleReference {
    abstract override val name: String
}

data class DomainModuleReference internal constructor(override val name: String) : FunctionalModuleReference()
data class LibraryModuleReference internal constructor(override val name: String) : FunctionalModuleReference()

data class BundleModuleReference internal constructor(override val name: String) : ModuleReference

data class ComponentReference internal constructor(val name: String)
