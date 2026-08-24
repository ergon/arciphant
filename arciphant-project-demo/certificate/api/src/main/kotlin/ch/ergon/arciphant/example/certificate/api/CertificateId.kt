package ch.ergon.arciphant.example.certificate.api

import ch.ergon.arciphant.example.shared.api.Id
import java.util.*

@JvmInline
value class CertificateId(override val value: UUID) : Id
