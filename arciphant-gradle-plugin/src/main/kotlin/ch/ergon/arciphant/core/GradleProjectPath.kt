package ch.ergon.arciphant.core

internal data class GradleProjectPath private constructor(val projectNames: List<String>) {

    constructor(vararg projectNames: String) : this(projectNames.filter { it.isNotEmpty() })

    val value by lazy { projectNames.joinToString(separator = ":", prefix = ":") }

}
