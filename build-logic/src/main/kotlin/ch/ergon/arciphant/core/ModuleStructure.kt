package ch.ergon.arciphant.core

internal data class ModuleStructure(
    val modules: List<Module>,
) {
    val libraries by lazy { modules.filterIsInstance<LibraryModule>() }
}
