package ch.ergon.arciphant.example.exam.webapi

import ch.ergon.arciphant.example.shared.webapi.DisciplineDto
import java.util.*

data class ExamDto(val id: UUID, val discipline: DisciplineDto, val title: String)
