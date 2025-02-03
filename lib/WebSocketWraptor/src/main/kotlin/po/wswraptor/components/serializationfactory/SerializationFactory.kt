package po.wswraptor.components.serializationfactory

import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import po.wswraptor.components.serializationfactory.interfaces.SerializeFactoryInterface
import po.wswraptor.components.serializationfactory.models.SerializerContainer
import po.wswraptor.models.request.WSMessage
import po.wswraptor.models.request.WSRequest
import po.wswraptor.models.response.WSResponse
import kotlin.reflect.KClass



class SerializationFactory<T: WSMessage<*>>(
    private val baseClass: KClass<T>,
) : CanNotify, SerializeFactoryInterface {

    override val eventHandler = RootEventHandler("SerializationFactory")
    val repository =  mutableMapOf<String, SerializerContainer<*>>()
    @OptIn(ExperimentalSerializationApi::class)
    val jsonConf = Json{
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        decodeEnumsCaseInsensitive = true
    }

    private fun <P: Any> getContainer(resourceName: String): SerializerContainer<P>{
        repository[resourceName]?.let{
            return it as SerializerContainer<P>
        }?:throwSkip("Repository does not contain any items for key : $resourceName").let { throw it }
    }
    private fun <P: Any>getContainer(type: TypeInfo): SerializerContainer<P> {
        repository.values.firstOrNull { it.typeInfo == type }?.let {
            return it as SerializerContainer<P>
        } ?: throwSkip("Repository does not contain any items with type : ${type.type}").let { throw it }
    }

    override fun  <C: Any>register(resourceName: String) = registerPayload<Any>(resourceName)

    inline fun <reified C: Any>registerPayload(resourceName: String){

       val serializer = serializer<C>()
        val type = typeInfo<C>()
        repository.putIfAbsent(resourceName,
            SerializerContainer(resourceName, type,  serializer)
        )
    }

   override fun <T:WSMessage<*>>serialize(typeInfo: TypeInfo, value : Any): String?{

       var serializedStr : String?

        getContainer<T>(typeInfo).let {
            val serializer = it.kSerializer
            serializedStr =   when(baseClass){
                 WSRequest::class  -> {
                    val wsRequest = value as?  WSRequest<T>
                    if(wsRequest == null){
                        null
                    }else{
                        val  baseSerializer = WSRequest.getSerializer<T>(serializer)
                        val serialized = jsonConf.encodeToString(baseSerializer, wsRequest)
                        serialized
                    }
                }
                WSResponse::class ->{
                    val wsResponse = value as?  WSResponse<T>
                    val  baseSerializer = WSResponse.getSerializer<T>(serializer)
                    val serialized = jsonConf.encodeToString(baseSerializer, wsResponse )
                    serialized
                }
                else -> {
                    null
                }
            }
        }
        return serializedStr
    }

    override fun <T: Any>deserialize(data: String, typeInfo: TypeInfo?): WSMessage<T>?{
        val container = if(typeInfo!=null){
           getContainer<T>(typeInfo)
        }else {
             val resource = jsonConf.parseToJsonElement(data).jsonObject["resource"]?.jsonPrimitive?.content
             resource?.let {
                 getContainer<T>(it)
             }
        }
        if(container!=null) {
            val baseSerializer =   WSRequest.getSerializer<T>(container.kSerializer)
            return jsonConf.decodeFromString(baseSerializer, data)
        }
        return null
    }
}