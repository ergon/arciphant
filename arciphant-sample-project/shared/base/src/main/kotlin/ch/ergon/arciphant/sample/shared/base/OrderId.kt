package ch.ergon.arciphant.sample.shared.base

import java.util.*

data class OrderId(override val value: UUID) : Id<UUID>
