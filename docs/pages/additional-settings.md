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
