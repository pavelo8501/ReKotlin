package po.lognotify.debug

import po.lognotify.TasksManaged
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.debug.extensions.createInputParameter
import po.lognotify.debug.interfaces.DebugContext
import po.lognotify.debug.models.CaptureBlock
import po.lognotify.debug.models.DebugParams
import po.lognotify.debug.models.InputParameter
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableContext


class DebugProxy<T: IdentifiableContext, P: PrintableBase<P>>(
    val receiver:T,
    val printableClass: PrintableCompanion<P>,
    private val dataProcessor: LoggerDataProcessor,
    val dataProvider: (DebugParams<P>)-> P
){

    var methodName: String = "N/A"
        set(value) {
            if(field != value){
                field = value
            }
        }

    val inputParams: MutableList<InputParameter> = mutableListOf()


    @PublishedApi
    internal fun logInput(inputParameter: InputParameter){

        val parametersStr = inputParams.joinToString(separator = "; ") {
              it.toString()
        }
        val message = "Method:$methodName; Input Parameters: $parametersStr"
        val printable =  dataProvider.invoke(DebugParams(message, null))
        dataProcessor.debug(printable, printableClass, null)
    }

    fun debug(message: String){
        val printable =  dataProvider.invoke(DebugParams(message, null))
        dataProcessor.debug(printable, printableClass, null)
    }

    fun debug(message: String, template: PrintableTemplate<P>){
        val printable =  dataProvider.invoke(DebugParams(message, template))
        dataProcessor.debug(printable, printableClass, template)
    }

    fun captureInput(vararg parameters: Any):DebugProxy<T,P>{
        parameters.forEachIndexed { index, parameter ->
            val inputParameter = createInputParameter(index, parameter)
            inputParams.add(inputParameter)
            logInput(inputParameter)
        }
        return this
    }

    inline fun <reified INPUT: Any> capture(parameter: INPUT, captureBlock:CaptureBlock<INPUT>.()-> Unit):DebugProxy<T,P>{
        captureInput(parameter)
        val param = inputParams.first()
        val block = CaptureBlock(parameter, param)
        captureBlock.invoke(block)
        block.inputParams.forEach {
            param.addListParameter(it)
        }
        logInput(param)
        return this
    }

    fun inputParameters(): List<InputParameter>{
        return inputParams
    }
}

fun <T: IdentifiableContext, P: PrintableBase<P>> TasksManaged.debugProxy(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    dataProvider: (DebugParams<P>)-> P
):DebugProxy<T, P>{
    val dataProcessor = this.logHandler
    return  DebugProxy(receiver,printableClass, dataProcessor,  dataProvider)
}