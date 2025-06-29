package po.misc.data.printable

import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableProxy.ProxyParams
import po.misc.interfaces.IdentifiableContext

class PrintableProxy<T: Any, D: PrintableBase<D>>(
   val receiver: T,
   val defaultTemplate: PrintableTemplate<D>,
   val dataBuilder: T.(ProxyParams<D>)-> Unit
){
    data class ProxyParams<D: PrintableBase<D>>(
        val message: String,
        val template:PrintableTemplate<D>
    )

    fun logMessage(message: String){
       dataBuilder.invoke(receiver, ProxyParams(message, defaultTemplate))
    }

    fun logMessage(message: String, template: PrintableTemplate<D>){
        dataBuilder.invoke(receiver, ProxyParams(message, template))
    }
}

fun<T: Any, D: PrintableBase<D>>  IdentifiableContext.printableProxy(
    holder:T,
    defaultTemplate: PrintableTemplate<D>,
    dataBuilder: T.(ProxyParams<D>)->Unit
):PrintableProxy<T, D>{
    return  PrintableProxy(holder, defaultTemplate, dataBuilder)
}