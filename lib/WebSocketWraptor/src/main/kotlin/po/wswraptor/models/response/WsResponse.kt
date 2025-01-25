package po.wswraptor.models.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import po.wswraptor.models.request.ApiRequestDataType
import po.wswraptor.models.request.WsRequest


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
data class WsResponse<T>(
    @Transient private val parentRequest: WsRequest<ApiRequestDataType>? = null,
    val result : T? = null
){

    var ok: Boolean = true
    var msg: String = ""
    var errorCode: Int = 0

    val request: String

    init {
        request = parentRequest?.requestJson?:""
    }

    fun setErrorMessage(errorCode: Int, message: String):WsResponse<T>{
        ok = false
        msg = message
        this.errorCode = errorCode
        return this
    }

}