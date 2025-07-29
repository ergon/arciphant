package ch.ergon.arciphant.example.shared.web

import ch.ergon.arciphant.example.shared.domain.Discipline
import ch.ergon.arciphant.example.shared.webapi.DisciplineDto

fun Discipline.toApi() = DisciplineDto(
    id = id.value,
    title = title,
)
