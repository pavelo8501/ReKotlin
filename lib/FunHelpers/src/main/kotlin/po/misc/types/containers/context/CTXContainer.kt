package po.misc.types.containers.context


open class CTXContainer<S: Any>(
   val source: S
){

}

fun <S: Any> S.toCTXContainer():CTXContainer<S>{
    return CTXContainer(this)
}


class ValueContainer<S: Any>(
    private val source: S
){
    val value:S get() = source
}

fun <S: Any> S.toValueContainer():ValueContainer<S>{
    return ValueContainer(this)
}


//open class LambdaContainer<T, R>(
//    internal val source: T
//) where T: CtxId, R: Any{
//
//    var actionLambda: (suspend T.()->R)? = null
//
//    open fun provideLambda(block:suspend T.()->R){
//        actionLambda = block
//    }
//
//    fun extract():T{
//        return source
//    }
//}



