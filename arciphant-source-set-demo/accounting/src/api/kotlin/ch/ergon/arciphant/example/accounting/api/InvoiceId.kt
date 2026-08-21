package ch.ergon.arciphant.example.accounting.api

import ch.ergon.arciphant.example.shared.api.Id
import java.util.*

@JvmInline
value class InvoiceId(override val value: UUID) : Id
