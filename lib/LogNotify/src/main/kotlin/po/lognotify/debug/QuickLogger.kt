package po.lognotify.debug


import po.misc.interfaces.CtxId
import po.misc.interfaces.IdentifiableContext

class EmptyContext<T:CtxId>(val ctx:T):IdentifiableContext, LoggedContext<T>{
    override val contextName: String = "LoggedContext"
}


class TemplateBuilder<T: Any>(internal val receiver:T){
    val parts: MutableList<PrintPart<*>> = mutableListOf()

    data class PrintPart<T: Any>(
        internal val receiver:T,
        val key: String,
        val value: String
    ){
        fun extract():T{
            return receiver
        }
    }

//
//   inline fun <reified T: Any>  log(receiver:T):PrintPart<T>{
//        val name = receiver::class.simpleName.toString()
//        val value = receiver.toString()
//        val part = PrintPart(receiver, name, value)
//        parts.add(part)
//       return part
//    }
}

interface LoggedContext<T: CtxId> {

    fun output(value: Any) {
        println(value)
    }

    fun <T: Collection<*>> quantity(block:()->T): T {
        val result = block.invoke()
        println(result.size)
        return result
    }

//    fun < T: Any> asTemplatePart(
//        block: (T) -> TemplateBuilder.PrintPart<*>
//    ): Unit{
//
//        val result =  block.invoke()
//        parts.add(result)
//    }
//
//    fun  logInHeader(block: T.()-> TemplateBuilder.PrintPart<T>){
//        val part =  block.invoke(receiver)
//        parts.add(part)
//    }
//    fun extract():T{
//        return receiver
//    }
}

inline fun <T: CtxId, R: Any> T.explore(
    block: LoggedContext<T>.()-> R
):R {
    val empty = EmptyContext(this)
    val result = block.invoke(empty)
    return result
}

inline fun <reified T:  Any> T.withHeader(
    block: TemplateBuilder<T>.()-> Unit
): Unit{
    val template = TemplateBuilder(this)
    block.invoke(template)
}
