package ch.ergon.arciphant.example.exam.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ExamServiceTest {

    private val examRepositoryMock = mock<ExamRepository>()

    private val examService = ExamService(repository = examRepositoryMock)

    @Test
    fun `it should load exams`() {
        whenever(examRepositoryMock.getExams()).thenReturn(listOf(ExamFixtures.anyExam))

        val exams = examService.getExams()

        assertThat(exams).containsExactlyInAnyOrder(ExamFixtures.anyExam)
    }

}
