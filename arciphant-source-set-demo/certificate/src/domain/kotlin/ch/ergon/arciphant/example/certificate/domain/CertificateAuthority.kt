package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.course.api.CourseId

interface CertificateAuthority {

    fun issueCertificate(courseId: CourseId): Certificate

}
