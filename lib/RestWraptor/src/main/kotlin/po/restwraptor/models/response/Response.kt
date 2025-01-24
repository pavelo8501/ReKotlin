package po.restwraptor.models.response

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
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

    fun setErrorMessage(errorCode: Int, message: String):Response<T>{
        ok = false
        msg = message
        this.errorCode = errorCode
        return this
    }
}