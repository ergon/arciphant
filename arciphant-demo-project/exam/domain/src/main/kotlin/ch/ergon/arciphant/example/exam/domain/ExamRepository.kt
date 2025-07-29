package ch.ergon.arciphant.example.exam.domain

import ch.ergon.arciphant.example.exam.api.ExamId

interface ExamRepository {

    fun addExam(exam: Exam)

    fun getExam(id: ExamId): Exam

    fun getExams(): List<Exam>

}
