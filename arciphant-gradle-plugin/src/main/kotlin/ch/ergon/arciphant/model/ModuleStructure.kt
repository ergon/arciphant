package ch.ergon.arciphant.model

internal data class ModuleStructure(
    val modules: List<Module>,
) {
    val libraries by lazy { modules.filterIsInstance<LibraryModule>() }
}
