package po.misc.collections


fun <T: Any> List<T>.exactlyOneOrThrow(exceptionProvider:()-> Throwable):T{
    if(size != 1){
       throw exceptionProvider()
    }else{
        return this[0]
    }
}

fun <T: Any> T?.asList(): List<T>{

   return if (this != null){
        listOf(this)
    }else{
        emptyList<T>()
    }
}



