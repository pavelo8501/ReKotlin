package po.misc.collections


fun <T: Any> List<T>.exactlyOneOrThrow(exceptionProvider:()-> Throwable):T{
    if(size != 1){
       throw exceptionProvider()
    }else{
        return this[0]
    }
}