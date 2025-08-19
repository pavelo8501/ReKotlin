package po.restwraptor.interfaces


interface WraptorResponse<T: Any> {
    val status: Boolean
    val message: String
    val errorCode: Int

    fun setErrorMessage(msg: String, errorCode: Int):WraptorResponse<T>
}

