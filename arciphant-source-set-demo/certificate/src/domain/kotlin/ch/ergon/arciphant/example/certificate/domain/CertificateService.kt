package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.course.api.CourseId
import org.springframework.stereotype.Service

@Service
class CertificateService(
    private val certificateAuthority: CertificateAuthority,
    private val certificateFileStore: CertificateFileStore,
    private val certificateRepository: CertificateRepository,
) {

    fun issueAndPersistCertificate(courseId: CourseId) {
        val certificate = certificateAuthority.issueCertificate(courseId)
        certificateFileStore.persistCertificateDocument(certificate.id, certificate.document)
        certificateRepository.addCertificate(certificate)
    }

}
