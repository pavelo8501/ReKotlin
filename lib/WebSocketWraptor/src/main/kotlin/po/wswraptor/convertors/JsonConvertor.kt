package po.wswraptor.convertors

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.reifiedType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import po.wswraptor.components.serializationfactory.interfaces.SerializeFactoryInterface
import po.wswraptor.models.response.WSResponse


data class JsonConvertorConfig(
   val inboundFactory : SerializeFactoryInterface,
   val outboundFactory : SerializeFactoryInterface,
   val json: Json? = null
)


@OptIn(ExperimentalSerializationApi::class)
class JsonConvertor (private val config : JsonConvertorConfig) : WebsocketContentConverter{

    override suspend fun deserialize(
        charset: io.ktor.utils.io.charsets.Charset,
        typeInfo: TypeInfo,
        content: Frame
    ): Any? {

        val frameText = (content as Frame.Text).readText()
        return config.inboundFactory.deserialize<Any>(frameText, typeInfo)
    }

    override suspend fun serialize(
        charset: io.ktor.utils.io.charsets.Charset,
        typeInfo: TypeInfo,
        value: Any?) : Frame {

        if(value==null){
            throw SerializationException("Data received is null")
        }

        val encodedString =  config.outboundFactory.serialize<WSResponse<Any>>(typeInfo, value)
        if(encodedString!=null){
            return Frame.Text(encodedString)
        }
        return Frame.Text("Unsupported response type")
    }

    override fun isApplicable(frame: Frame): Boolean {
        if(frame is Frame.Text){
            return true
        }
        return false
    }
}