package ch.ergon.arciphant.example.shared.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import kotlin.reflect.KClass

fun MockMvc.postWithRequestBody(url: String, body: Any) = post(url) {
    contentType = APPLICATION_JSON
    content = objectMapper().writeValueAsString(body)
}

fun ResultActionsDsl.responseBodyAsString(): String = andReturn().response.contentAsString

fun <T : Any> String.deserializeList(type: KClass<T>): List<T> =
    objectMapper().readerForListOf(type.java).readValue(this)

fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
