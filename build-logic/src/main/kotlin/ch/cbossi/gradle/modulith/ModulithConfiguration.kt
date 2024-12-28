package ch.cbossi.gradle.modulith

internal data class ModulithConfiguration(
    val modules: List<Module>,
    val bundleModules: List<BundleModule>,
) {
    val libraries by lazy { modules.filterIsInstance<LibraryModule>() }
}
