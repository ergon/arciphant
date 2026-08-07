package ch.ergon.arciphant

import ch.ergon.arciphant.ArciphantPlugin.Companion.EXTENSION_NAME
import ch.ergon.arciphant.dsl.ArciphantProjectDsl
import ch.ergon.arciphant.sca.verify
import org.gradle.api.Plugin
import org.gradle.api.Project

class ArciphantProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            val dsl = extensions.create(EXTENSION_NAME, ArciphantProjectDsl::class.java)
            afterEvaluate { dsl.verify(path) }
        }
    }
}
