import ch.ergon.arciphant.example.exam.api.ExamId
import ch.ergon.arciphant.example.exam.api.ExamIdFixtures
import ch.ergon.arciphant.example.exam.db.ExamRepositoryImpl
import ch.ergon.arciphant.example.exam.domain.Exam
import ch.ergon.arciphant.example.exam.domain.ExamFixtures
import ch.ergon.arciphant.example.shared.api.DisciplineIdFixtures
import ch.ergon.arciphant.example.shared.db.AbstractInMemoryRepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExamRepositoryImplTest :
    AbstractInMemoryRepositoryTest<ExamRepositoryImpl, Exam, ExamId>(
        repository = ExamRepositoryImpl(),
        addRecordFn = ExamRepositoryImpl::addExam,
        getRecordFn = ExamRepositoryImpl::getExam,
        record = ExamFixtures.anyExam,
        idFn = Exam::id
    ) {

    @Test
    fun `it should get exams`() {
        val exam1 = ExamFixtures.anyExam
        val exam2 = Exam(ExamIdFixtures.random(), DisciplineIdFixtures.any, "Professional Level")
        repository.addExam(exam1)
        repository.addExam(exam2)

        assertThat(repository.getExams()).containsExactlyInAnyOrder(exam1, exam2)
    }

}
