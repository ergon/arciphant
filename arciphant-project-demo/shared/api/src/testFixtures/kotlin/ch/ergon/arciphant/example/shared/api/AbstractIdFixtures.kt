package ch.ergon.arciphant.example.shared.api

import java.util.*

abstract class AbstractIdFixtures<ID : Id>(private val constructor: (UUID) -> ID) {

    val any = random()

    fun random() = of(UUID.randomUUID())

    fun of(uuid: UUID) = constructor(uuid)

}
