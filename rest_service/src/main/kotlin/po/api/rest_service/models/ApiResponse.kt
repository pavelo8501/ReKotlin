package po.api.rest_service.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class ApiResponse<T>(
     val data : T? = null,
){
    var ok: Boolean = false
    var msg: String = ""
    var errorCode: Int = 0

    init {
        if(data != null){
            ok = true
            msg = "ok"
            errorCode = 0
        }
    }

    fun setErrorMessage(errorCode: Int, message: String):ApiResponse<T>{
        ok = false
        msg = message
        this.errorCode = errorCode
        return this
    }
}