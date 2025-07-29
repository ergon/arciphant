package ch.ergon.arciphant.example.exam.web

import ch.ergon.arciphant.example.exam.domain.ExamFixtures
import ch.ergon.arciphant.example.exam.domain.ExamService
import ch.ergon.arciphant.example.exam.webapi.ExamDto
import ch.ergon.arciphant.example.shared.domain.DisciplineFixtures
import ch.ergon.arciphant.example.shared.domain.DisciplineRepository
import ch.ergon.arciphant.example.shared.web.IsolatedWebMvcTest
import ch.ergon.arciphant.example.shared.web.deserializeList
import ch.ergon.arciphant.example.shared.web.responseBodyAsString
import ch.ergon.arciphant.example.shared.web.toApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@IsolatedWebMvcTest(ExamControllerImpl::class)
class ExamControllerImplTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var disciplineRepositoryMock: DisciplineRepository

    @MockitoBean
    private lateinit var examServiceMock: ExamService

    @Test
    fun `it should get exams`() {
        val exam = ExamFixtures.anyExam
        val discipline = DisciplineFixtures.anyDiscipline
        whenever(examServiceMock.getExams()).thenReturn(listOf(exam))
        whenever(disciplineRepositoryMock.getDisciplineById(exam.disciplineId)).thenReturn(discipline)

        val exams = mockMvc.get("/exams")
            .andExpect { status { isOk() } }
            .responseBodyAsString().deserializeList(ExamDto::class)

        assertThat(exams).containsExactlyInAnyOrder(ExamDto(
            id = exam.id.value,
            discipline = discipline.toApi(),
            title = exam.title,
        ))
    }

}
