package ch.ergon.arciphant.example.certificate.filestore

import ch.ergon.arciphant.example.certificate.api.CertificateId
import ch.ergon.arciphant.example.certificate.api.CertificateIdFixtures
import ch.ergon.arciphant.example.shared.filestore.AbstractFileStoreTest

class CertificateFileStoreImplTest : AbstractFileStoreTest<CertificateFileStoreImpl, CertificateId>(
    CertificateFileStoreImpl(),
    CertificateFileStoreImpl::persistCertificateDocument,
    CertificateFileStoreImpl::getCertificateDocument,
    CertificateIdFixtures.any,
)
