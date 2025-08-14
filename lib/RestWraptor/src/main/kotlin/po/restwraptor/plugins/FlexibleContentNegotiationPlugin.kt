package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.toByteArray
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import po.misc.types.getOrThrow
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.exceptions.configException
import kotlin.reflect.jvm.jvmErasure


private fun trySingle(text: String,  serializer : KSerializer<out Any>, formatter: Json): Any?{
    return try {
        formatter.decodeFromString(serializer, text.trim())
    }catch (ex: SerializationException){
        null
    }
}

private fun tryList(text: String,  serializer :   KSerializer<out List<Any>>, formatter: Json): List<Any>?{
    return try {
        formatter.decodeFromString(serializer, text.trim())
    }catch (ex: SerializationException){
        null
    }
}


@OptIn(InternalSerializationApi::class)
val FlexibleContentNegotiationPlugin = createApplicationPlugin("FlexibleContentNegotiationPlugin") {

    val jsonFormatter = Json {
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    onCallReceive { call ->

        val asText = call.toString()


        transformBody {
           val transformBodyContext = this
           val byteReadChannel = it
           val type = transformBodyContext.requestedType.getOrThrow(this){msg->
               configException(msg.message, ExceptionCodes.VALUE_IS_NULL)
           }

           val byteChannel = it.toString()
           if (type == String::class) return@transformBody it.toString() // skip plain text
           val text = (it.toByteArray()).decodeToString()
           val serializer =  type.kotlinType?.jvmErasure?.serializer()
           val decoded =  jsonFormatter.decodeFromString(serializer!!, text.trim())
           trySingle(text, serializer, jsonFormatter) ?:run {
              val result =  tryList(text,ListSerializer(serializer), jsonFormatter)
           }
        }
    }
}