package ch.ergon.arciphant.example.course.domain

import ch.ergon.arciphant.example.course.api.CourseId
import ch.ergon.arciphant.example.shared.api.DisciplineId

data class Course(val id: CourseId, val disciplineId: DisciplineId, val title: String)
