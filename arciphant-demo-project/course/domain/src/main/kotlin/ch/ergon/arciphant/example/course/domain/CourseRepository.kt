package ch.ergon.arciphant.example.course.domain

import ch.ergon.arciphant.example.course.api.CourseId

interface CourseRepository {

    fun addCourse(course: Course)

    fun getCourse(id: CourseId): Course

    fun getCourses(): List<Course>

}
