package ch.ergon.arciphant.sample.shared.webapi

import ch.ergon.arciphant.sample.shared.base.Id
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

@Configuration
internal class IdApiTypeConfiguration(private val idApiTypes: List<IdApiType<*>>) : WebMvcConfigurer {

    /**
     * Required to use ID classes in request/response payload
     */
    @Bean
    fun jacksonBuilder(): Jackson2ObjectMapperBuilder {
        val module = SimpleModule()
        idApiTypes.forEach { it.registerSerializerAndDeserializer(module) }
        return Jackson2ObjectMapperBuilder().modulesToInstall(module)
    }

    /**
     * Required to use ID classes in @PathVariable arguments
     */
    override fun addFormatters(registry: FormatterRegistry) {
        super.addFormatters(registry)
        idApiTypes.forEach { it.registerConverter(registry) }
    }


}

internal abstract class UuidApiType<ID : Id<UUID>>(
    idClass: Class<ID>,
    constructor: (UUID) -> ID
) : IdApiType<ID>(idClass, { constructor(UUID.fromString(it)) })

internal abstract class IdApiType<ID : Id<*>>(
    private val idClass: Class<ID>,
    private val constructor: (String) -> ID
) {

    fun registerSerializerAndDeserializer(module: SimpleModule) {
        module.addSerializer(idClass, IdSerializer())
        module.addDeserializer(idClass, object : IdDeserializer<ID>(idClass, constructor) {})
    }

    fun registerConverter(registry: FormatterRegistry) {
        registry.addConverter(String::class.java, idClass, object : IdConverter<ID>(constructor) {})
    }

}

private class IdSerializer : StdSerializer<Id<*>>(Id::class.java) {
    override fun serialize(value: Id<*>, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(value.value.toString())
    }
}

private abstract class IdDeserializer<ID : Id<*>>(
    idType: Class<ID>,
    private val constructor: (String) -> ID
) : StdDeserializer<ID>(idType) {

    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ID? {
        return jsonParser.valueAsString?.let { constructor(it) }
    }
}

private abstract class IdConverter<ID>(private val constructor: (String) -> ID) : Converter<String, ID> {
    override fun convert(source: String): ID {
        return constructor(source)
    }
}
