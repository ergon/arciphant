import org.gradle.api.tasks.SourceSetContainer

val sourceSets = extensions.getByType<SourceSetContainer>()

arciphant {
    sourceSetDependencies {
        // the certificate domain uses the exam module's public API
        api(
            sourceSet = sourceSets.getByName("domain"),
            projectPath = ":exam",
            componentName = "api",
        )
    }
}
