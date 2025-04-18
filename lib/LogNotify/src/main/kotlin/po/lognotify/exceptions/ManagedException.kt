package po.lognotify.exceptions

import po.lognotify.exceptions.enums.HandlerType
import java.util.Objects


sealed interface SelfThrownException<T:ManagedException> {
    val message: String
    var handler  : HandlerType
    val builderFn: (String) -> T
}


abstract class ManagedException(
    override val message: String,
    override var handler  : HandlerType,
) : Throwable(message), SelfThrownException<ManagedException>{

    private var source : Throwable? = null
    fun setSourceException(th: Throwable): ManagedException{
        source = th
        return this
    }

}





//sealed class ExceptionBase(
//    override val message: String,
//    open var handler: HandlerType,
//    errorCode : Int = 0
//) : Throwable(message), SelfThrownException{
//
//
//    private var source : Throwable? = null
//    fun setSourceException(th: Throwable): ExceptionBase{
//        source = th
//        return this
//    }
//    fun reThrowSource(){
//        if(source !=null){
//            throw source!!
//        }else{
//            throw Exception("Rethrow Source exception failed. No source set in containing ${handler.toString()} with message $message")
//        }
//    }
//}