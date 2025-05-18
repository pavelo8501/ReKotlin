package po.misc.reflection

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


interface MetaContainer{
    companion object{
        val snapshot: MutableMap<String, KProperty1<*, Any?>> = mutableMapOf()
    }
}

inline fun <reified T:MetaContainer> T.LogOnFault(block: (T.()-> Unit)):T{
    this::class.memberProperties.forEach {
        MetaContainer.snapshot[it.name] = it
    }
    block.invoke(this).apply {
        this::class.memberProperties.forEach {
            MetaContainer.snapshot[it.name] = it
        }
    }
    return this
}

inline fun <reified T> RegisterForLogging(noinline block: ()-> T){

    val lambda : ()-> T = block

    val stop = lambda.invoke()

    val a = 10
}