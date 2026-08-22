package ch.ergon.arciphant.example.shared.web

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ContextConfiguration
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

@Target(CLASS)
@Retention(RUNTIME)
@WebMvcTest
@ContextConfiguration(classes = [TestConfiguration::class])
annotation class IsolatedWebMvcTest(
    @get:AliasFor(annotation = ContextConfiguration::class, attribute = "classes")
    vararg val classes: KClass<*> = [],
)
