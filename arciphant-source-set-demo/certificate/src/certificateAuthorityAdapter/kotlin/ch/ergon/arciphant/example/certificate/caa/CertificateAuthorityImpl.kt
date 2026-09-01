package ch.ergon.arciphant.example.certificate.caa

import ch.ergon.arciphant.example.certificate.api.CertificateId
import ch.ergon.arciphant.example.certificate.domain.Certificate
import ch.ergon.arciphant.example.certificate.domain.CertificateAuthority
import ch.ergon.arciphant.example.exam.api.ExamResultId
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class CertificateAuthorityImpl : CertificateAuthority {

    override fun issueCertificate(examResultId: ExamResultId): Certificate {
        return Certificate(
            id = CertificateId(UUID.randomUUID()),
            document = File.createTempFile("certificate", ".pdf"),
        )
    }
}
