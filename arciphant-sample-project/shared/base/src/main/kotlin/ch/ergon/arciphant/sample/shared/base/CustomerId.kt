package ch.ergon.arciphant.sample.shared.base

import java.util.UUID

data class CustomerId(override val value: UUID) : Id<UUID>
