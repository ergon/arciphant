package ch.ergon.arciphant.example.certificate.web

import ch.ergon.arciphant.example.certificate.domain.CertificateService
import ch.ergon.arciphant.example.certificate.webapi.CertificateController
import ch.ergon.arciphant.example.certificate.webapi.IssueCertificateDto
import ch.ergon.arciphant.example.exam.api.ExamResultId
import org.springframework.web.bind.annotation.RestController

@RestController
class CertificateControllerImpl(
    private val certificateService: CertificateService
) : CertificateController {
    override fun issueCertificate(issueCertificate: IssueCertificateDto) {
        certificateService.issueAndPersistCertificate(ExamResultId(issueCertificate.examResultId))
    }
}
