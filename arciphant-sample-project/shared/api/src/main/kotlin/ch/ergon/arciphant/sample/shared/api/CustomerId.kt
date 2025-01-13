package ch.ergon.arciphant.sample.shared.api

import ch.ergon.arciphant.sample.shared.base.Id
import java.util.UUID

data class CustomerId(override val value: UUID) : Id<UUID>
