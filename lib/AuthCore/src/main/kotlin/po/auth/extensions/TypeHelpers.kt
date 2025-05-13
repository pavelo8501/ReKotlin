package po.auth.extensions


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
