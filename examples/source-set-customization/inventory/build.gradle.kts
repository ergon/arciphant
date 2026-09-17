plugins {
    `java-library`
}

// relocates the source directories of every component from the default 'src/<sourceSet>/java' layout
// to '<component>/java', '<component>/test/java' and '<component>/testFixtures/java'
customizeAllComponents {
    productionSourceSet { sourceSet, componentName ->
        sourceSet.java.setSrcDirs(listOf("$componentName/java"))
        sourceSet.resources.setSrcDirs(listOf("$componentName/resources"))
    }
    testSourceSet { sourceSet, componentName ->
        sourceSet.java.setSrcDirs(listOf("$componentName/test/java"))
    }
    testFixturesSourceSet { sourceSet, componentName ->
        sourceSet.java.setSrcDirs(listOf("$componentName/testFixtures/java"))
    }
}

// a single component can be customized further: the 'web' component additionally compiles generated sources
customizeComponent("web") {
    productionSourceSet { sourceSet ->
        sourceSet.java.srcDir("$componentName/generated/java")
    }
}

dependencies {
    // Arciphant's shared configurations: reaches the test source sets of all components
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
