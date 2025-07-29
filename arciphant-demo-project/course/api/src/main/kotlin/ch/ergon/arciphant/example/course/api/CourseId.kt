package ch.ergon.arciphant.example.course.api

import ch.ergon.arciphant.example.shared.api.Id
import java.util.*

@JvmInline
value class CourseId(override val value: UUID) : Id
