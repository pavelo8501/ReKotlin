package po.restwraptor.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse<T: Any?>(
    val data : T? = null,
){
    var status: Boolean = true
    var message: String = "ok"
    @SerialName("error_code")
    var errorCode: Int = 0

    fun setErrorMessage(msg: String, errorCode: Int):ApiResponse<T>{
        status = false
        message = msg
        this.errorCode = errorCode
        return this
    }

    companion object{
        fun withErrorMessage(msg: String, errorCode:Int):ApiResponse<Nothing>{
            return  ApiResponse<Nothing>().setErrorMessage(msg, errorCode)
        }
    }
}