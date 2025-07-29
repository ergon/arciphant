package ch.ergon.arciphant.example.course.domain

import ch.ergon.arciphant.example.course.api.CourseIdFixtures
import ch.ergon.arciphant.example.shared.api.DisciplineIdFixtures

object CourseFixtures {

    val anyCourse = Course(
        id = CourseIdFixtures.any,
        disciplineId = DisciplineIdFixtures.any,
        title = "Gradle for Beginner"
    )

}
