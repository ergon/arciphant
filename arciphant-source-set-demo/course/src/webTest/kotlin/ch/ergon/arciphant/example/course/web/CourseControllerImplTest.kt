package ch.ergon.arciphant.example.course.web

import ch.ergon.arciphant.example.course.domain.CourseFixtures
import ch.ergon.arciphant.example.course.domain.CourseService
import ch.ergon.arciphant.example.course.webapi.CourseDto
import ch.ergon.arciphant.example.shared.domain.DisciplineFixtures
import ch.ergon.arciphant.example.shared.domain.DisciplineRepository
import ch.ergon.arciphant.example.shared.web.IsolatedWebMvcTest
import ch.ergon.arciphant.example.shared.web.deserializeList
import ch.ergon.arciphant.example.shared.web.responseBodyAsString
import ch.ergon.arciphant.example.shared.webapi.DisciplineDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@IsolatedWebMvcTest(CourseControllerImpl::class)
class CourseControllerImplTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var disciplineRepositoryMock: DisciplineRepository

    @MockitoBean
    private lateinit var courseServiceMock: CourseService

    @Test
    fun `it should get exams`() {
        val course = CourseFixtures.anyCourse
        val discipline = DisciplineFixtures.anyDiscipline
        whenever(courseServiceMock.getCourses()).thenReturn(listOf(course))
        whenever(disciplineRepositoryMock.getDisciplineById(course.disciplineId)).thenReturn(discipline)

        val courses = mockMvc.get("/courses")
            .andExpect { status { isOk() } }
            .responseBodyAsString().deserializeList(CourseDto::class)

        assertThat(courses).containsExactlyInAnyOrder(
            CourseDto(
                id = course.id.value,
                discipline = DisciplineDto(id = course.disciplineId.value, title = discipline.title),
                title = course.title,
            )
        )
    }

}
