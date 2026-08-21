package ch.ergon.arciphant.example.shared.db

import ch.ergon.arciphant.example.shared.api.DisciplineId
import ch.ergon.arciphant.example.shared.domain.Discipline
import ch.ergon.arciphant.example.shared.domain.DisciplineFixtures

internal class DisciplineRepositoryImplTest :
    AbstractInMemoryRepositoryTest<DisciplineRepositoryImpl, Discipline, DisciplineId>(
        repository = DisciplineRepositoryImpl(),
        addRecordFn = DisciplineRepositoryImpl::addDiscipline,
        getRecordFn = DisciplineRepositoryImpl::getDisciplineById,
        record = DisciplineFixtures.anyDiscipline,
        idFn = Discipline::id
    )
