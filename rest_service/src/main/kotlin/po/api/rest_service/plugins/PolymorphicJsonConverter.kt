package po.api.rest_service.plugins

import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.nio.charset.Charset
import kotlin.reflect.KType


class PolymorphicJsonConverter (private val json: Json) : ContentConverter{
    private fun getSerializerForType(type: KType): KSerializer<Any> {
        @Suppress("UNCHECKED_CAST")
        return json.serializersModule.serializer(type) as KSerializer<Any>
    }

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        if (value == null) return null

        val serializer = getSerializerForType(typeInfo.kotlinType!!)
        val jsonText = json.encodeToString(serializer, value)
        return TextContent(jsonText, contentType.withCharset(charset))
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
        val text = content.readRemaining().readText(charset)
        val serializer = getSerializerForType(typeInfo.kotlinType!!)
        return json.decodeFromString(serializer, text)
    }
}