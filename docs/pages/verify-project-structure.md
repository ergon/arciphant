---
icon: lucide/package-check
---

# Verify project structure

Typically in a multi-project setup, you want to have different packages for the different projects. An example:

| Gradle project path                     | Absolute package name                             |
|-----------------------------------------|---------------------------------------------------|
| `:certificate:domain`                   | `com.company.project.certificate.domain`          |
| `:certificate:web-api`                  | `com.company.project.package.certificate.webapi`  |
| `:accounting:domain`                    | `com.company.project.package.accounting.domain`   |
| `:accounting:web-api`                   | `com.company.project.package.accounting.webapi`   |
| `:accounting:payment-provider-adapter`  | `com.company.project.package.accounting.ppa`      |

Arciphant provides the task `validatePackageStructure` verify correct package structure. 
The task is only available in the root project but scans all the subprojects, too.

To configure it, add a `packageStructureValidation` block to the *arciphant* configuration, for example:

``` kotlin title="settings.gradle.kts" hl_lines="4-6"
arciphant {
  [..]
  
  packageStructureValidation {
    basePackageName("ch.ergon.arciphant.example")
  }
}

```

If no configuration is provided, default values are applied.

## Configuration options

The `packageStructureValidation` task provides the following configuration options (see also `PackageStructureValidationDsl`) to customize the desired package structure:

| Option                                                          | Description                                                                                                                                                                                                                                                                        |
|-----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <span class="nowrap">`basePackageName`</span>                   | The base package name for the whole project.<br/>Example: `basePackageName('com.company.project')`                                                                                                                                                                                 |
| <span class="nowrap">`disableUseLowerCase`</span>               | <p>By default, upper case letters are converted to lower case when mapping project names to corresponding package fragments.</p><p>Example: project name `FileStore` is mapped to package fragment `filestore`.</p><p>Use `disableUseLowerCase()` to deactivate this behavior.</p> |
| <span class="nowrap">`disableRemoveUnderscore`</span>           | <p>By default, underscores '_' are removed when mapping project name to corresponding package fragment.</p><p>Example: project name `file_store` is mapped to package fragment `filestore`.</p><p>Use `disableRemoveUnderscore()` to deactivate this behavior.</p>                 |
| <span class="nowrap">`disableRemoveHyphen`</span>               | <p>By default, hyphens '-' are removed when mapping project name to corresponding package fragment.</p><p>Example: project name `file-store` is mapped to package fragment `filestore`.</p><p>Use `disableRemoveHyphen` to deactivate this behavior.</p>                           |
| <span class="nowrap">`mapProjectNamesToPackageFragments`</span> | Configure mappings for specific project names. See detailed description [below](#mapprojectnamestopackagefragments).                                                                                                                                                               |
| <span class="nowrap">`mapProjectPathsToAbsolutePackages`</span> | Completely overrides the package name for the given Gradle project path. See detailed description [below](#mapprojectpathstoabsolutepackages).                                                                                                                                                                       |
| <span class="nowrap">`excludeProjectPath`</span>                | <p>Excludes a specific project from package validation.</p><p>Example: `excludeProjectPath(:specific:project:path)`</p>                                                                                                                                                            |
| <span class="nowrap">`excludeResourcesFolder`</span>            | <p>By default, all folders in the `src`-folder of each project are validated.</p><p>Use `excludeResourcesFolder()` to exclude the resource folder (`src/main/resources`) from validation.</p>                                                                                      |
| <span class="nowrap">`excludeSrcFolders`</span>                 | <p>By default, all folders in the src-folder of each project are validated.</p><p>Use `excludeSrcFolders()` to exclude specific folders.</p><p>Example: To exclude `src/main/generated` use: `excludedSrcFolder("main/generated")`</p>                                             |

### `mapProjectNamesToPackageFragments`

Configure mappings for specific project names. The project name can be either a leaf project (e.g., an arciphant component) or a parent project (e.g., an arciphant module). The `basePackageName` is still used. The configured value replaces only the package fragment related to the specified project.

Example:
``` kotlin title="settings.gradle.kts" hl_lines="7-10"
arciphant {
  [..]
  
  packageStructureValidation {
    basePackageName("com.company.project")
    
    mapProjectNameToPackageFragment(
      "financial-accounting" to "accounting",
      "payment-provider-adapter", "ppa",
    )
  }
}
```

The above config results in the following mapping:

| Gradle project path                              | Absolute package name                   |
|--------------------------------------------------|-----------------------------------------|
| `:financial-accounting:domain`                   | `com.company.project.accounting.domain` |
| `:financial-accounting:web-api`                  | `com.company.project.accounting.webapi` |
| `:financial-accounting:payment-provider-adapter` | `com.company.project.accounting.ppa`    |

### `mapProjectPathsToAbsolutePackages`

Completely overrides the package name for the given Gradle project path.
Other than with `mapProjectNamesToPackageFragments`, the `basePackageName` is NOT used.

Example:

``` kotlin title="settings.gradle.kts" hl_lines="7-10"
arciphant {
  [..]
  
  packageStructureValidation {
    basePackageName("com.company.project")
    
    mapProjectPathToAbsolutePackage(
      ":specific:project:path" to "com.specific.package.name",
      ":any:other:path" to "com.any.other.package.name",
    )
  }
}
```
