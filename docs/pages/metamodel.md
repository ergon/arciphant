---
icon: lucide/layout-panel-top
---

# Arciphant metamodel

The following diagram shows the conceptual metamodel of Arciphant. See [Declare Structure](declare-structure.md) for how modules and components can be created.

``` mermaid
---    
config:
    class:
        hideEmptyMembersBox: true
---
classDiagram
    class Module {
        name: String
    }
    
    class FunctionalModule

    class DomainModule
    class LibraryModule

    class BundleModule {
        pluginId: String?
    }

    class Component {
        name: String
        pluginId: String?
    }

    class Dependency {
        type: API | IMPLEMENTATION
    }

    FunctionalModule "1" *-- "n" Component : components

    Component "1" *-- "n" Dependency : dependsOn
    Dependency -- "1" Component: component

    Module <|-- FunctionalModule

    FunctionalModule <|-- LibraryModule
    FunctionalModule <|-- DomainModule

    BundleModule "1" *-- "n" Module: includes

    Module <|-- BundleModule
    
    classDef default fill:#eef0fe,stroke:#526cfe
```
