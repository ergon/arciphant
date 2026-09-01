package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.exam.api.ExamResultId

interface CertificateAuthority {

    fun issueCertificate(examResultId: ExamResultId): Certificate

}
