package ch.cbossi.gradle.modulith

internal data class ModulithConfiguration(
    val modules: List<ComponentBasedModule>,
    val bundles: List<BundleModule>,
) {
    val libraries by lazy { modules.filterIsInstance<LibraryModule>() }
}
