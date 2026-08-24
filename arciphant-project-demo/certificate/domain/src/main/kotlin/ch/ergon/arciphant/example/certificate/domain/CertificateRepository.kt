package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.certificate.api.CertificateId

interface CertificateRepository {

    fun addCertificate(certificate: Certificate)

    fun getCertificate(id: CertificateId): Certificate

}
