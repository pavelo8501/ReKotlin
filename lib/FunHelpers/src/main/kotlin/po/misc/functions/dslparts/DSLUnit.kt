package po.misc.functions.dslparts


interface DSLConstructor<T: Any,V: Any, R: Any>{

   // val accept: T.(V)->R
    abstract class DSLBlock<R: Any>()

    abstract class DSLParameter<V: Any>()
}


abstract class DSLBlock<R: Any>()

abstract class DSLParameter<V: Any>()


class DSLUnit<T: Any, V: Any, R: Any>(
    val dslLambda:T.(V)->R
) {

}

