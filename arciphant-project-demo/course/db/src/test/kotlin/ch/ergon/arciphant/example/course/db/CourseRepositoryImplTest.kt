package ch.ergon.arciphant.example.course.db

import ch.ergon.arciphant.example.course.api.CourseId
import ch.ergon.arciphant.example.course.api.CourseIdFixtures
import ch.ergon.arciphant.example.course.domain.Course
import ch.ergon.arciphant.example.course.domain.CourseFixtures
import ch.ergon.arciphant.example.shared.api.DisciplineIdFixtures
import ch.ergon.arciphant.example.shared.db.AbstractInMemoryRepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CourseRepositoryImplTest :
    AbstractInMemoryRepositoryTest<CourseRepositoryImpl, Course, CourseId>(
        repository = CourseRepositoryImpl(),
        addRecordFn = CourseRepositoryImpl::addCourse,
        getRecordFn = CourseRepositoryImpl::getCourse,
        record = CourseFixtures.anyCourse,
        idFn = Course::id
    ) {

    @Test
    fun `it should get courses`() {
        val course1 = CourseFixtures.anyCourse
        val course2 = Course(CourseIdFixtures.random(), DisciplineIdFixtures.any, "Advanced Kotlin")
        repository.addCourse(course1)
        repository.addCourse(course2)

        assertThat(repository.getCourses()).containsExactlyInAnyOrder(course1, course2)
    }

}
