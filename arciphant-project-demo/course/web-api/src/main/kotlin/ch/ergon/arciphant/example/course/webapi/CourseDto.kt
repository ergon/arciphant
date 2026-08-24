package ch.ergon.arciphant.example.course.webapi

import ch.ergon.arciphant.example.shared.webapi.DisciplineDto
import java.util.*

data class CourseDto(val id: UUID, val discipline: DisciplineDto, val title: String)
