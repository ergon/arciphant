package ch.ergon.arciphant.core

internal data class GradleProjectPath private constructor(val projectNames: List<String>) {

    init {
        require(projectNames.none { it.isEmpty() }) {
                "Cannot create ${GradleProjectPath::class.simpleName} with empty project names: $projectNames" }
    }

    val value by lazy { projectNames.joinToString(separator = ":", prefix = ":") }

    companion object {
        fun of(projectNames: List<String>) = GradleProjectPath(projectNames.filter { it.isNotEmpty() })
    }

}
