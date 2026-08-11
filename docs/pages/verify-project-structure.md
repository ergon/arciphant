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
| <span class="nowrap">`excludeProjectPath`</span>                | <p>Excludes a specific project from package validation.</p><p>Example: `excludeProjectPath(:specific:project:path)`</p>                                                                                                                                                            |
| <span class="nowrap">`excludeResourcesFolder`</span>            | <p>By default, all folders in the `src`-folder of each project are validated.</p><p>Use `excludeResourcesFolder()` to exclude the resource folder (`src/main/resources`) from validation.</p>                                                                                      |
| <span class="nowrap">`excludeSrcFolders`</span>                 | <p>By default, all folders in the src-folder of each project are validated.</p><p>Use `excludeSrcFolders()` to exclude specific folders.</p><p>Example: To exclude `src/main/generated` use: `excludedSrcFolder("main/generated")`</p>                                             |

## Project-specific overrides

The expected package of a single project can be overridden in the project's own `build.gradle.kts`.
Arciphant registers the extension `arciphant` (type `ArciphantProjectDsl`) on every project for this purpose.

### `packageName`

Overrides the package fragment of the project. The project can be either a leaf project (e.g., an arciphant component) or a parent project (e.g., an arciphant module). The `basePackageName` and the package fragments of the parent projects are still used. The configured value replaces only the package fragment derived from the project's name.

Example:
``` kotlin title="financial-accounting/payment-provider-adapter/build.gradle.kts"
import ch.ergon.arciphant.dsl.ArciphantProjectDsl

configure<ArciphantProjectDsl> {
    packageName = "ppa"
}
```

With `basePackageName("com.company.project")`, the above config results in the following mapping:

| Gradle project path                              | Absolute package name                            |
|--------------------------------------------------|--------------------------------------------------|
| `:financial-accounting:domain`                   | `com.company.project.financialaccounting.domain` |
| `:financial-accounting:payment-provider-adapter` | `com.company.project.financialaccounting.ppa`    |

An empty string removes the fragment of the project from the expected package.

### `absolutePackageName`

Completely overrides the expected package of the project.
Other than with `packageName`, neither the `basePackageName` nor the package fragments of the parent projects are used.

The override is inherited by child projects: their expected package is the configured absolute package name extended by their own package fragments (including possible `packageName` overrides). A child project can replace an inherited absolute package name by configuring its own `absolutePackageName`.

Example:

``` kotlin title="backend/accounting/build.gradle.kts"
import ch.ergon.arciphant.dsl.ArciphantProjectDsl

configure<ArciphantProjectDsl> {
    absolutePackageName = "my.special"
}
```

The above config results in the following mapping (regardless of the configured `basePackageName`):

| Gradle project path            | Configuration in `build.gradle.kts`      | Absolute package name |
|--------------------------------|------------------------------------------|-----------------------|
| `:backend:accounting`          | `absolutePackageName = "my.special"`     | `my.special`          |
| `:backend:accounting:domain`   | —                                        | `my.special.domain`   |
| `:backend:accounting:web-api`  | `packageName = "foo"`                    | `my.special.foo`      |
| `:backend:accounting:db`       | `absolutePackageName = "org.other.db"`   | `org.other.db`        |
