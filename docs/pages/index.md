---
icon: lucide/book-open
---

# Introduction

## About Arciphant

Arciphant is a Gradle plugin that allows to specify the module structure of complex software project declaratively using a simple DSL:

* Module templates define the technical structure (components and their dependencies) of modules
* These templates can be configured using a simple DSL directly in the Gradle settings file
* Modules can be instantiated based on templates and further components can be added as required

This offers various advantages:

* Architecture styles (e.g. Clean Architecture) can be mapped cleanly and declaratively as a Gradle Multi-Project Build
* The boilerplate part in the Gradle code is minimized
* The effort required to introduce a new module is negligible
* No further tools (such as ArchUnit) are required to define the basic architectural units (modules and components) and map their dependencies
* Thanks to the interaction with the java-test-fixtures plugin (optional), testing dependencies work out-of-the-box without further effort

Arciphant offers the greatest benefit for software projects that consist of many different modules, whereby these modules have the same or a similar technical structure (e.g. Clean Architecture, Hexagonal Architecture, Onion Architecture, Layered Architecture) as, for example, in a modulith architecture.


## Technologies

The Arciphant Gradle Plugin is written in Kotlin.

![image](https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![image](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)

## License

Arciphant is released under the [MIT License](https://github.com/ergon/arciphant/blob/main/LICENSE){ target="_blank"}.

![image](https://img.shields.io/badge/MIT-green?style=for-the-badge)

## Maintenance

The publisher (Ergon Informatik AG) does *NOT* guarantee active maintenance and further development.
We only work on the plugin to the extent that we need for our own projects.

## About the name

The name Arciphant is made up of architecture and elephant, whereby elephant is to be understood as a metaphor for a large project and at the same time is a reference to the Gradle logo.

![Logo](images/logo.png){ style="width: 300px; max-width: 100%"}