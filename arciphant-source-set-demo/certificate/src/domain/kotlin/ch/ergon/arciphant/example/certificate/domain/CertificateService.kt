package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.exam.api.ExamResultId
import org.springframework.stereotype.Service

@Service
class CertificateService(
    private val certificateAuthority: CertificateAuthority,
    private val certificateFileStore: CertificateFileStore,
    private val certificateRepository: CertificateRepository,
) {

    fun issueAndPersistCertificate(examResultId: ExamResultId) {
        val certificate = certificateAuthority.issueCertificate(examResultId)
        certificateFileStore.persistCertificateDocument(certificate.id, certificate.document)
        certificateRepository.addCertificate(certificate)
    }

}
