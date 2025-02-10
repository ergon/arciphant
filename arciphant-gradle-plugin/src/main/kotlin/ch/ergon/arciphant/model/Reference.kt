package ch.ergon.arciphant.model

internal sealed interface NamedReference {
    val name: String
}

sealed interface ModuleReference

sealed class FunctionalModuleReference : ModuleReference, NamedReference {
    abstract override val name: String
}

data class DomainModuleReference internal constructor(override val name: String) : FunctionalModuleReference()
data class LibraryModuleReference internal constructor(override val name: String) : FunctionalModuleReference()

internal sealed interface BundleModuleReference : ModuleReference
internal data class ChildBundleModuleReference(override val name: String) : BundleModuleReference, NamedReference
internal object RootBundleModuleReference : BundleModuleReference

data class ComponentReference internal constructor(override val name: String) : NamedReference
