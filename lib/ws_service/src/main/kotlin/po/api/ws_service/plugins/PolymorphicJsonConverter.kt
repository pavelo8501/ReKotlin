package po.api.ws_service.service.plugins

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.util.reflect.TypeInfo


import io.ktor.websocket.Frame
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class PolymorphicJsonConverter (private val json: Json) : WebsocketContentConverter{

    private val serializerCache = mutableMapOf<KType, KSerializer<Any>>()

    private fun getSerializerForType(type: KType): KSerializer<Any> {
        @Suppress("UNCHECKED_CAST")
        return serializerCache.getOrPut(type) {
            json.serializersModule.serializer(type) as KSerializer<Any>
        }
    }


    override suspend fun deserialize(
        charset: io.ktor.utils.io.charsets.Charset,
        typeInfo: TypeInfo,
        content: Frame
    ): Any? {
        if(content is Frame.Text){

            //val baseApiResponse = json.decodeFromString<WSApiRequest<Any>>(content.readText())
            val a = 10
        }
        return null
    }

    override fun isApplicable(frame: Frame): Boolean {
        TODO("Not yet implemented")
    }
}