package po.api.ws_service.service.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class ServiceResponse(var serviceMessage : String = "" ){

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
data class WSApiResponse<T>(val result : T? = null) {

    var ok: Boolean = true
    var msg: String = ""
    var errorCode: Int = 0

    fun setErrorMessage(errorCode: Int, message: String):WSApiResponse<T>{
        ok = false
        msg = message
        this.errorCode = errorCode
        return this
    }

}