package po.misc.data.printable

import po.misc.context.CTX
import po.misc.data.printable.PrintableProxy.ProxyParams
import po.misc.data.printable.companion.PrintableTemplateBase

class PrintableProxy<T: Any, D: PrintableBase<D>>(
    val receiver: T,
    val defaultTemplate: PrintableTemplateBase<D>,
    val dataBuilder: T.(ProxyParams<D>)-> Unit
){
    data class ProxyParams<D: PrintableBase<D>>(
        val message: String,
        val template: PrintableTemplateBase<D>
    )

    fun logMessage(message: String){
       dataBuilder.invoke(receiver, ProxyParams(message, defaultTemplate))
    }

    fun logMessage(message: String, template: PrintableTemplateBase<D>){
        dataBuilder.invoke(receiver, ProxyParams(message, template))
    }
}

fun <T: Any, D: PrintableBase<D>>  CTX.printableProxy(
    holder:T,
    defaultTemplate: PrintableTemplateBase<D>,
    dataBuilder: T.(ProxyParams<D>)->Unit
):PrintableProxy<T, D>{
    return  PrintableProxy(holder, defaultTemplate, dataBuilder)
}