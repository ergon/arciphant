package ch.ergon.arciphant.example.exam.domain

import ch.ergon.arciphant.example.course.api.CourseId
import ch.ergon.arciphant.example.exam.api.ExamId
import ch.ergon.arciphant.example.shared.api.DisciplineId

data class Exam(val id: ExamId, val courseId: CourseId, val disciplineId: DisciplineId, val title: String)
