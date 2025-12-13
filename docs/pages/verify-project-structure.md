---
icon: lucide/package-check
---

# Verify project structure

Typically in a multi-project setup, you want to have different packages for the different projects.
Arciphant provides a task to verify correct package structure: `validatePackageStructure`.
The task is only available in the root project, but scans all the subprojects, too.

To configure the task, add a `packageStructureValidation` block to the *arciphant* configuration in `settings.gradle.kts`. Example:

``` kotlin title="settings.gradle.kts"
packageStructureValidation {
    basePackageName("ch.ergon.arciphant.example")
    
    mapProjectNamesToPackageFragments("module-a" to "aaa")
    
    mapProjectPathsToAbsolutePackages(":any:special:project-name" to "ch.ergon.arciphant.example.special")
}
```

If no configuration is provided, default values are applied.

See `PackageStructureValidationDsl` for available configurations and their default values.
