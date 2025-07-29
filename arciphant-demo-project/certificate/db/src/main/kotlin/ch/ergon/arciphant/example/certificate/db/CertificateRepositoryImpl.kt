package ch.ergon.arciphant.example.certificate.db

import ch.ergon.arciphant.example.certificate.api.CertificateId
import ch.ergon.arciphant.example.certificate.domain.Certificate
import ch.ergon.arciphant.example.certificate.domain.CertificateRepository
import ch.ergon.arciphant.example.shared.db.InMemoryRepository
import org.springframework.stereotype.Repository

@Repository
class CertificateRepositoryImpl : CertificateRepository, InMemoryRepository<Certificate, CertificateId>() {

    override fun addCertificate(certificate: Certificate) {
        addRecord(certificate.id, certificate)
    }

    override fun getCertificate(id: CertificateId): Certificate {
        return getRecord(id)
    }
}
