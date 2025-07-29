package ch.ergon.arciphant.example.shared.db

import ch.ergon.arciphant.example.shared.api.DisciplineId
import ch.ergon.arciphant.example.shared.domain.Discipline
import ch.ergon.arciphant.example.shared.domain.DisciplineRepository
import org.springframework.stereotype.Repository

@Repository
internal class DisciplineRepositoryImpl : DisciplineRepository, InMemoryRepository<Discipline, DisciplineId>() {

    override fun addDiscipline(discipline: Discipline) {
        addRecord(discipline.id, discipline)
    }

    override fun getDisciplineById(id: DisciplineId): Discipline {
        return getRecord(id)
    }

}
