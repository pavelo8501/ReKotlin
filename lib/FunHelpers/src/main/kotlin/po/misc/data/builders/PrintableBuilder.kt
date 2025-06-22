package po.misc.data.builders

import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplateBase
import po.misc.interfaces.IdentifiableContext

class LogProxy<T: Any, D: PrintableBase<D>>(
   val receiver: T,
   var template:  PrintableTemplateBase<D>?,
   val dataBuilder: T.(String)-> D
){

    fun logMessage(message: String):D{
        val dataRecord = dataBuilder.invoke(receiver, message)
        return dataRecord
    }
}

fun<D: PrintableBase<D>, T: IdentifiableContext> IdentifiableContext.logProxy(
    holder:T,
   dataBuilder: T.(String)->D
):LogProxy<T, D>{

    return  LogProxy(holder, null, dataBuilder)
}

inline  fun<T: Any, reified D: PrintableBase<D>> IdentifiableContext.logProxy(
    holder:T,
    template:  PrintableTemplateBase<D>,
    noinline dataBuilder: T.(String)->D
):LogProxy<T, D>{

    return  LogProxy(holder, template, dataBuilder)
}