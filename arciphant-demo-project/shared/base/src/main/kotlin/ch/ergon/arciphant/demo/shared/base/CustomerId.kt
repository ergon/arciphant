package ch.ergon.arciphant.demo.shared.base

import java.util.UUID

data class CustomerId(override val value: UUID) : Id<UUID>
