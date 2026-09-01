package ch.ergon.arciphant.example.exam.domain

import org.springframework.stereotype.Service

@Service
class ExamService(private val repository: ExamRepository) {

    fun getExams(): List<Exam> {
        return repository.getExams()
    }

}
