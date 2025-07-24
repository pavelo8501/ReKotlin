package po.misc.functions.common



class Fallback<T: Any>(
    valueFallback:(()-> T)? = null
){

    private var  fallbackWithValue:(()->T)? = null
    private var  fallbackWithThrowable:((String)-> Throwable)? = null

    var exceptionMessage: String = ""

    constructor(message: String,  exceptionProvider:(String)-> Throwable): this(null){

        exceptionMessage = message
        fallbackWithThrowable = exceptionProvider
    }

    init {
        fallbackWithValue = valueFallback
    }

    fun initiateFallback():T{
        val valueFallback = fallbackWithValue
        if(valueFallback != null){
          return  valueFallback.invoke()
        }
       throw fallbackWithThrowable?.invoke(exceptionMessage)?:run {
           throw  IllegalArgumentException("No value for fallback no Exception were provided")
       }
    }
}