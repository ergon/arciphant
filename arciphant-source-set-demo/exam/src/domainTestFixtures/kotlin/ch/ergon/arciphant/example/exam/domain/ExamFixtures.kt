package ch.ergon.arciphant.example.exam.domain

import ch.ergon.arciphant.example.exam.api.ExamIdFixtures
import ch.ergon.arciphant.example.shared.api.DisciplineIdFixtures

object ExamFixtures {

    val anyExam = Exam(
        id = ExamIdFixtures.any,
        disciplineId = DisciplineIdFixtures.any,
        title = "Foundation Level"
    )

}
