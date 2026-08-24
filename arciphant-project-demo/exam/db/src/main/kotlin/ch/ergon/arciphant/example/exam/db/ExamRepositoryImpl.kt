package ch.ergon.arciphant.example.exam.db

import ch.ergon.arciphant.example.exam.api.ExamId
import ch.ergon.arciphant.example.exam.domain.Exam
import ch.ergon.arciphant.example.exam.domain.ExamRepository
import ch.ergon.arciphant.example.shared.db.InMemoryRepository
import org.springframework.stereotype.Repository

@Repository
class ExamRepositoryImpl : ExamRepository, InMemoryRepository<Exam, ExamId>() {

    override fun addExam(exam: Exam) {
        addRecord(exam.id, exam)
    }

    override fun getExam(id: ExamId): Exam {
        return getRecord(id)
    }

    override fun getExams(): List<Exam> {
        return getRecords()
    }
}
