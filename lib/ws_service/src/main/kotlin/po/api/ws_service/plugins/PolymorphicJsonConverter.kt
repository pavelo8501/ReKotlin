package po.api.ws_service.service.plugins

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.util.reflect.TypeInfo


import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import po.api.ws_service.service.models.ApiRequestDataType
import po.api.ws_service.service.models.CreateRequest
import po.api.ws_service.service.models.DeleteRequest
import po.api.ws_service.service.models.SelectRequest
import po.api.ws_service.service.models.WSApiRequest
import po.api.ws_service.service.models.WSApiRequestBase
import po.api.ws_service.services.ConnectionService
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ApiRequestDataTypeDeserializer<D>() : JsonContentPolymorphicSerializer<ApiRequestDataType>(ApiRequestDataType::class) {
    var dataSerializer: ((String) -> KSerializer<*>)? = null
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ApiRequestDataType> {
        var type = element.jsonObject["type"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'type' discriminator")
//        type = element.jsonObject["type"]?.jsonPrimitive?.content
//            ?: throw SerializationException("module parameter is missing")
        return when (type) {
            "select" -> SelectRequest.serializer()
            "delete" -> DeleteRequest.serializer()
            else -> {
                CreateRequest.serializer(dataSerializer!!.invoke(type))
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
class PolymorphicJsonConverter (private val connectionService: ConnectionService, json: Json? = null) : WebsocketContentConverter{

    private var jsonFormat: Json

    init {
        if(json == null){
            val jsonFormat = Json {
               ignoreUnknownKeys = true
               encodeDefaults = true
               isLenient = true
               decodeEnumsCaseInsensitive = true
            }
            this.jsonFormat = jsonFormat
        }else{
            this.jsonFormat = json
        }
    }


    private val serializerCache = mutableMapOf<String, KSerializer<Any>>()

    private fun <T>getSerializerForResource(path: String, filedName:String): KSerializer<T>?{

    //    val kTypeInfo =  connectionService.getWSMethod(path,filedName)?.typeInfo

        val kTypeInfo =   connectionService.getWSMethod(path,filedName)?.typeInfo

        if(kTypeInfo == null){
           throw SerializationException("Type info not found for $path|$filedName")
        }

        val serializer = serializer(kTypeInfo)
        @Suppress("UNCHECKED_CAST")
        return serializer as KSerializer<T>?
    }

    fun getSerializer(path: String, method:String): KSerializer<Any>{
        var serializer = serializerCache["$path|$method"]
        if(serializer == null){
            serializer = getSerializerForResource(path, method)
        }
        return serializer!!
    }

    override suspend fun deserialize(
        charset: io.ktor.utils.io.charsets.Charset,
        typeInfo: TypeInfo,
        content: Frame
    ): Any? {
        val info = typeInfo
        var currentRequestMethod : String? = null
        val apiRequestDataTypeSerializer = ApiRequestDataTypeDeserializer<ApiRequestDataType>()
        val wsApiRequestSerializer = WSApiRequest.serializer(apiRequestDataTypeSerializer)
        apiRequestDataTypeSerializer.dataSerializer = {
            currentRequestMethod = it
            getSerializer("partners",it)
        }

        val frameText = (content as Frame.Text).readText()
        val apiRequest = try {
                 jsonFormat.decodeFromString(wsApiRequestSerializer,frameText)
        }catch (e: Exception){
            throw WebsocketDeserializeException(e.message?:"Unknown error",e.cause,content)
        }
        return apiRequest
    }


//    override suspend fun serialize(
//        charset: io.ktor.utils.io.charsets.Charset,
//        typeInfo: TypeInfo,
//        content: Frame
//    ): Any? {
//
//        val info = typeInfo
//
//        if(content is Frame.Text){
//            val frameText = content.readText()
//            val baseApiResponse = json.decodeFromString<WSApiRequest>(content.readText())
//            val a = 10
//        }
//        return null
//    }

    override fun isApplicable(frame: Frame): Boolean {
        if(frame is Frame.Text){
            return true
        }
        return false
    }
}