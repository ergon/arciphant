package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.certificate.api.CertificateIdFixtures
import java.io.File

object CertificateFixtures {

    val anyCertificate = Certificate(
        id = CertificateIdFixtures.any,
        document = File.createTempFile("any", ".pdf"),
    )

}
