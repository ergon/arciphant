package ch.ergon.arciphant.example.exam.webapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("exams")
interface ExamController {

    @GetMapping
    fun getExams(): List<ExamDto>
}
