package ch.ergon.arciphant.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArciphantSampleApplication

fun main(args: Array<String>) {
    runApplication<ArciphantSampleApplication>(*args)
}