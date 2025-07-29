package ch.ergon.arciphant.example.exam.web

import ch.ergon.arciphant.example.exam.domain.Exam
import ch.ergon.arciphant.example.exam.domain.ExamService
import ch.ergon.arciphant.example.exam.webapi.ExamController
import ch.ergon.arciphant.example.exam.webapi.ExamDto
import ch.ergon.arciphant.example.shared.domain.DisciplineRepository
import ch.ergon.arciphant.example.shared.web.toApi
import org.springframework.web.bind.annotation.RestController

@RestController
class ExamControllerImpl(
    private val examService: ExamService,
    private val disciplineRepository: DisciplineRepository,
) : ExamController {

    override fun getExams(): List<ExamDto> {
        return  examService.getExams().map { it.toApi() }
    }

    private fun Exam.toApi() = ExamDto(
        id = id.value,
        discipline = disciplineRepository.getDisciplineById(disciplineId).toApi(),
        title = title,
    )
}
