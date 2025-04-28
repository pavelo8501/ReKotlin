package po.auth.extensions

import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.misc.exceptions.castOrException
import po.misc.exceptions.getOrException

typealias Predicate<T> = (T) -> Boolean
data class CallbackContainer<T>(
   private val receiver:T,
){

    fun onSuccess(block: T.()-> Unit){
         block.invoke(receiver)
    }

    fun predicate(predicate: Predicate<T>): Boolean{
       predicate.invoke(receiver).let {
            if (it) {
                ::onSuccess
               return true
            }
        }
        return false
    }
}




//internal fun <T> T?.getOrThrow(message: String, code: ErrorCodes):T{
//    return this.getOrException(){
//        throw AuthException(message, code)
//    }
//}

//inline fun <reified T: Any> Any.castOrThrow(message: String = "", code: ErrorCodes = ErrorCodes.SESSION_PARAM_FAILURE): T {
//    return this.castOrException {
//        val exception = AuthException(message, code)
//        exception.addMessage("$message. Cast from  ${T::class.simpleName} to  ${this::class.simpleName}  failed ")
//    }
//}


fun <T>  T.testAndLet(predicate: Predicate<T>, block: CallbackContainer<T>.()-> Unit){

     val container =  CallbackContainer(this)
     if(container.predicate(predicate)) {
         container.block()
     }
}


inline fun <T>  T.ifTestPass(predicate: (T)-> Boolean):T?{

    return if(predicate(this)){
        this
    }else{
        null
    }
}
