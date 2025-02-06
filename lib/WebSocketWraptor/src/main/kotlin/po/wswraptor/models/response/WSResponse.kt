package po.wswraptor.models.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import po.wswraptor.components.serializationfactory.SerializationFactory
import po.wswraptor.models.request.ApiRequestAction
import po.wswraptor.models.request.WSMessage
import po.wswraptor.models.request.WSMessageBase


@Serializable
data class ServiceResponse(var serviceMessage : String = ""){

    var ok : Boolean = true
    var errorCode : Int? = null

    fun setErrorMessage(message: String, errorCode: Int):ServiceResponse{
        ok = false
        this.serviceMessage = message
        this.errorCode = errorCode
        return this
    }

    fun toJson():String{
        return Json.encodeToString(this)
    }
}

@Serializable
data class WSResponse<P: Any>(
    override val payload : P? = null,
    override val resource: String,
    override val action: ApiRequestAction
): WSMessage<P>("Request"), WSMessageBase<P>{

    var ok: Boolean = true
    var msg: String = ""
    var errorCode: Int = 0

    fun setErrorMessage(errorCode: Int, message: String):WSResponse<P>{
        ok = false
        msg = message
        this.errorCode = errorCode
        return this
    }

    fun <T: Any>getSerializer(payloadSerializer : KSerializer<T>) = Companion.getSerializer(payloadSerializer)

    companion object {
        fun <T: Any>getSerializer(payloadSerializer : KSerializer<T>): KSerializer<WSResponse<T>>{
            return WSResponse.serializer(payloadSerializer)
        }
        inline fun <reified T : WSResponse<Any>> createFactory(
        ): SerializationFactory<T> {
            return SerializationFactory(T::class)
        }
    }

}