package ch.ergon.arciphant.example.course.web

import ch.ergon.arciphant.example.course.domain.Course
import ch.ergon.arciphant.example.course.domain.CourseService
import ch.ergon.arciphant.example.course.webapi.CourseController
import ch.ergon.arciphant.example.course.webapi.CourseDto
import ch.ergon.arciphant.example.shared.domain.DisciplineRepository
import ch.ergon.arciphant.example.shared.web.toApi
import org.springframework.web.bind.annotation.RestController

@RestController
class CourseControllerImpl(
    private val courseService: CourseService,
    private val disciplineRepository: DisciplineRepository,
): CourseController {

    override fun getCourses(): List<CourseDto> {
        return courseService.getCourses().map { it.toApi() }
    }

    private fun Course.toApi() = CourseDto(
        id = id.value,
        discipline = disciplineRepository.getDisciplineById(disciplineId).toApi(),
        title = title,
    )
}
