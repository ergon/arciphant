---
icon: lucide/settings
---

# Additional settings

## Custom base path

If your module/component structure does not start at the root project level, you can specify a base path. For example if your application is located in a sub-project `backend`:

``` kotlin title="settings.gradle.kts" hl_lines="2"
arciphant {
    basePath = "backend"
    
    [..]
}
```

The base path can also be a nested path (e.g. `backend:my-service`).

## Automatic folder creation

By default, Arciphant creates folders for all configured module components if they do not exist yet. You can disable this behavior:

``` kotlin title="settings.gradle.kts" hl_lines="2"
arciphant {
    disableFolderCreation()
    
    [..]
}
```

!!! warning "Gradle >= 9 requires project folders to be present"
    
    Creating folders for each project is especially important for projects using Gradle versions >= 9.
    Gradle 9 requires the project folder to be present in order for the project to be configurable (which is necessary for Arciphant to work properly).
    If you disable this feature, you are responsible yourself for creation all the necessary folders for your modules and components.

## Qualified archive base name

With Arciphant, we typically have multiple gradle projects with the same name, since all the modules have the same component projects. This can lead to problems when bundling module components together, since the generated jar file of those components has the same name, too. Without declaring a *duplicate handling strategy* in the build, this leads to an error:
```
Caused by: org.gradle.api.InvalidUserCodeException: Entry BOOT-INF/lib/db-plain.jar is a duplicate but no duplicate handling strategy has been set.
```

To overcome this issue, Arciphant automatically changes the default `archiveBaseName` for task `org.gradle.jvm.tasks.Jar` to a qualified name `moduleName-componentName` (e.g., in case of a component `domain` of `module-a`, the resulting base name would be `module-a-domain`).

If you want to *disable* this feature, you can do so in the Arciphant configuration:
``` kotlin title="settings.gradle.kts" hl_lines="2"
arciphant {
    disableQualifiedArchiveBaseName()
    
    [..]
}
```

!!! warning "Duplicate name handling strategy required"

    But be aware that if you disable it, you have to take care of preventing duplicated names yourself. To solve the issue in a generic way for arbitrary gradle projects, use can use something like this:
    ``` kotlin title="build.gradle.kts" hl_lines="2-4"
    subprojects {
        tasks.withType<Jar>().configureEach {
            archiveBaseName.set(project.path.removePrefix(":").replace(':', '-'))
        }
    }
    ```
