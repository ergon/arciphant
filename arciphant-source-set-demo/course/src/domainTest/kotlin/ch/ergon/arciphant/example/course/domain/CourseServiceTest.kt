package ch.ergon.arciphant.example.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CourseServiceTest {

    private val courseRepositoryMock = mock<CourseRepository>()

    private val courseService = CourseService(repository = courseRepositoryMock)

    @Test
    fun `it should load courses`() {
        whenever(courseRepositoryMock.getCourses()).thenReturn(listOf(CourseFixtures.anyCourse))

        val courses = courseService.getCourses()

        assertThat(courses).containsExactlyInAnyOrder(CourseFixtures.anyCourse)
    }

}
