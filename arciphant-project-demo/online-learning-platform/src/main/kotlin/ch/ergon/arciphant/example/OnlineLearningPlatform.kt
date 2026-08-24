package ch.ergon.arciphant.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArciphantDemoApplication

fun main(args: Array<String>) {
    runApplication<ArciphantDemoApplication>(*args)
}
