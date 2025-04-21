package po.auth.extensions




fun <T>  T.ifTestPass(predicate: (T)-> Boolean, block:()-> Unit){
    if(predicate(this)){
        block()
    }
}

fun <T, R>  T.ifNotNull (block:()-> R){
    if(this != null){
        block()
    }
}

suspend fun <T>  T.ifNull (block: suspend  ()-> T): T{
    if(this == null){
        return block()
    }
    return this
}