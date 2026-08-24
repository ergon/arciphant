package ch.ergon.arciphant.example.course.domain

import org.springframework.stereotype.Service

@Service
class CourseService(private val repository: CourseRepository) {

    fun getCourses(): List<Course> {
        return repository.getCourses()
    }

}
