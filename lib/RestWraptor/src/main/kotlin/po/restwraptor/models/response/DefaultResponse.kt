package po.restwraptor.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.restwraptor.interfaces.WraptorResponse


@Serializable
data class DefaultResponse<T: Any>(
    val data : T,
): WraptorResponse<T>{
    override var status: Boolean = true
    override var message: String = "ok"
    @SerialName("error_code")
    override var errorCode: Int = 0


    override fun setErrorMessage(msg: String, errorCode: Int):DefaultResponse<T>{
        status = false
        message = msg
        this.errorCode = errorCode
        return this
    }
}