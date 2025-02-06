package po.restwraptor.models.response

import kotlinx.serialization.Serializable



@Serializable
data class ServiceResponse(
    val message : String,
    val errorCode: Int,
)


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


@Serializable
data class PostResponse(
    var ok: Boolean = false,
    var msg: String = "",
    var errorCode: Int = 0,
)