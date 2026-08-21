package ch.ergon.arciphant.example.course.webapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("courses")
interface CourseController {

    @GetMapping
    fun getCourses(): List<CourseDto>

}
