package ch.ergon.arciphant.util

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

abstract class SimpleTask : DefaultTask() {

    @get:Internal
    protected val projectPath: String get() = path.removeSuffix(":$name").ifEmpty { ":" }

}