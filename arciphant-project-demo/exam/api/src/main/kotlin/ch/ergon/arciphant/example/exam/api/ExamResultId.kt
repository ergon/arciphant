package ch.ergon.arciphant.example.exam.api

import ch.ergon.arciphant.example.shared.api.Id
import java.util.*

@JvmInline
value class ExamResultId(override val value: UUID) : Id
