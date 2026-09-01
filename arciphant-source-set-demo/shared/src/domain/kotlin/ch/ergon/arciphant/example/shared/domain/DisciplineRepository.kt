package ch.ergon.arciphant.example.shared.domain

import ch.ergon.arciphant.example.shared.api.DisciplineId

interface DisciplineRepository {

    fun addDiscipline(discipline: Discipline)

    fun getDisciplineById(id: DisciplineId): Discipline

}
