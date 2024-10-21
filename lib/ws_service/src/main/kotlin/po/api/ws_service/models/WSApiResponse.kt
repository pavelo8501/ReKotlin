package po.api.ws_service.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import po.api.rest_service.models.ApiResponse
import po.api.rest_service.models.CreateRequestData


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
data class WSApiResponse<T>(val data : T? = null) {

}