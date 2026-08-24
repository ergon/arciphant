package ch.ergon.arciphant.example.certificate.filestore

import ch.ergon.arciphant.example.certificate.api.CertificateId
import ch.ergon.arciphant.example.certificate.domain.CertificateFileStore
import ch.ergon.arciphant.example.shared.filestore.FileStore
import org.springframework.stereotype.Service
import java.io.File

@Service
class CertificateFileStoreImpl : CertificateFileStore, FileStore() {

    override fun persistCertificateDocument(certificateId: CertificateId, certificateDocument: File) {
        addFile(certificateId, certificateDocument)
    }

    override fun getCertificateDocument(certificateId: CertificateId): File {
        return getFile(certificateId)
    }
}
