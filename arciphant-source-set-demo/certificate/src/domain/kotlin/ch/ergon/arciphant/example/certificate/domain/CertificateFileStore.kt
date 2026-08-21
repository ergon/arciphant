package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.certificate.api.CertificateId
import java.io.File

interface CertificateFileStore {

    fun persistCertificateDocument(certificateId: CertificateId, certificateDocument: File)
    fun getCertificateDocument(certificateId: CertificateId): File

}
