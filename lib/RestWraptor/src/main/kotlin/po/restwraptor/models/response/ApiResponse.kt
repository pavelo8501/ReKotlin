package po.restwraptor.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceResponse(
    var message: String,
    @SerialName("error_code")
    var errorCode: Int
){
    val ok: Boolean = false
    val log: MutableList<String> = mutableListOf<String>()
    fun addLogRecord(value : String){
        this.log.add(value)
    }
}

@Serializable
data class ApiResponse<T>(
    val data : T? = null,
){
    var status: Boolean = false
    var message: String = ""
    @SerialName("error_code")
    var errorCode: Int = 0

    init {
        if(data != null){
            status = true
            message = "ok"
            errorCode = 0
        }
    }

    fun setErrorMessage(errorCode: Int, msg: String):ApiResponse<T>{
        status = false
        message = msg
        this.errorCode = errorCode
        return this
    }
}