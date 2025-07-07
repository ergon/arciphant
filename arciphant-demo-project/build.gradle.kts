plugins {
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    plugins.apply("org.jetbrains.kotlin.plugin.spring")
    plugins.apply("org.springframework.boot")
    plugins.apply("io.spring.dependency-management")
}

tasks.register("projectDependencies") {
    doLast {
        allprojects.forEach { currentProject ->
            println("Project '${currentProject.path}'")
            currentProject.configurations.forEach { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java).forEach { dependency ->
                    if (dependency.path != currentProject.path) {
                        println(" |- [${configuration.name}] ${dependency.path}")
                    }
                }
            }
        }
    }
}
