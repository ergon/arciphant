package ch.ergon.arciphant.core

internal data class GradleProjectPath(val projectNames: List<String>) {

    constructor(vararg projectNames: String) : this(projectNames.toList())

    val value by lazy { projectNames.joinToString(separator = ":", prefix = ":") }

}
