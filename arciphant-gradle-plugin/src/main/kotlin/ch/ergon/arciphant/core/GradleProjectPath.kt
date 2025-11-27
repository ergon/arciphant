package ch.ergon.arciphant.core

internal data class GradleProjectPath private constructor(val projectNames: List<String>) {

    val value by lazy { projectNames.joinToString(separator = ":", prefix = ":") }

    companion object {
        fun of(projectNames: List<String>) = GradleProjectPath(projectNames.filter { it.isNotEmpty() })
    }

}
