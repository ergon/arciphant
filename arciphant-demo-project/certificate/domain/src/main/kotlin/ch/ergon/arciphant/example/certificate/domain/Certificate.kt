package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.certificate.api.CertificateId
import java.io.File

data class Certificate(val id: CertificateId, val document: File)
