package ch.ergon.arciphant.example.shared.domain

import ch.ergon.arciphant.example.shared.api.DisciplineId

data class Discipline(val id: DisciplineId, val title: String) {
    init {
        require(title.isNotBlank()) { "Discipline title must not be blank" }
    }
}
