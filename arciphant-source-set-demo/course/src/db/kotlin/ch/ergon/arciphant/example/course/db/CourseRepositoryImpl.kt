package ch.ergon.arciphant.example.course.db

import ch.ergon.arciphant.example.course.api.CourseId
import ch.ergon.arciphant.example.course.domain.Course
import ch.ergon.arciphant.example.course.domain.CourseRepository
import ch.ergon.arciphant.example.shared.db.InMemoryRepository
import org.springframework.stereotype.Repository

@Repository
class CourseRepositoryImpl : CourseRepository, InMemoryRepository<Course, CourseId>() {

    override fun addCourse(course: Course) {
        addRecord(course.id, course)
    }

    override fun getCourse(id: CourseId): Course {
        return getRecord(id)
    }

    override fun getCourses(): List<Course> {
        return getRecords()
    }
}
