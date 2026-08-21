package ch.ergon.arciphant.example.certificate.db

import ch.ergon.arciphant.example.certificate.api.CertificateId
import ch.ergon.arciphant.example.certificate.domain.Certificate
import ch.ergon.arciphant.example.certificate.domain.CertificateFixtures
import ch.ergon.arciphant.example.shared.db.AbstractInMemoryRepositoryTest

class CertificateRepositoryImplTest :
    AbstractInMemoryRepositoryTest<CertificateRepositoryImpl, Certificate, CertificateId>(
        repository = CertificateRepositoryImpl(),
        addRecordFn = CertificateRepositoryImpl::addCertificate,
        getRecordFn = CertificateRepositoryImpl::getCertificate,
        record = CertificateFixtures.anyCertificate,
        idFn = Certificate::id
    )
