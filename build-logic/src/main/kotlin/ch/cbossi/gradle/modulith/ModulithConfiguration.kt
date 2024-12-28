package ch.cbossi.gradle.modulith

internal data class ModulithConfiguration(
    val componentBasedModules: List<ComponentBasedModule>,
    val bundles: List<BundleModule>,
) {
    val modules by lazy { componentBasedModules + bundles }
    val libraries by lazy { componentBasedModules.filterIsInstance<LibraryModule>() }
}
