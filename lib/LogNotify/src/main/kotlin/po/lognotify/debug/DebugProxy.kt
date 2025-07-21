package po.lognotify.debug

import po.lognotify.TasksManaged
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.debug.interfaces.DebugProvider
import po.lognotify.debug.models.CaptureBlock
import po.lognotify.debug.models.DebugParams
import po.lognotify.debug.models.InputParameter
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.CTX
import po.misc.context.Identifiable


open class DebugProxy<T: CTX, P: PrintableBase<P>>(
    val receiver:T,
    val printableClass: PrintableCompanion<P>,
    private var dataProcessor: LoggerDataProcessor,
    val dataProvider: (DebugParams<P>)-> P
): DebugProvider{


    open var activeTemplate: PrintableTemplate<P>? = null
    var methodName: String = "N/A"
    override val inputParams: MutableList<InputParameter> = mutableListOf()

    @PublishedApi
    internal fun logInput(){
        val parametersStr = inputParams.joinToString(separator = "; ") {
              it.toString()
        }
        val message = "Method:$methodName; Input Parameters: $parametersStr"
        val printable =  dataProvider.invoke(DebugParams(message, null))
        dataProcessor.debug(printable, printableClass, activeTemplate)
    }

    @PublishedApi
    internal fun provideDataProcessor(processor:LoggerDataProcessor){
        methodName = processor.task.key.taskName
        dataProcessor = processor
        logInput()
    }

    fun notify(message: String){
        val printable =  dataProvider.invoke(DebugParams(message, null))
        dataProcessor.debug(printable, printableClass, null)
    }

    fun notify(message: String, template: PrintableTemplate<P>){
        val printable =  dataProvider.invoke(DebugParams(message, template))
        dataProcessor.debug(printable, printableClass, template)
    }

//    fun captureInput(vararg parameters: Any):DebugProxy<T,P>{
//        inputParams.clear()
//        parameters.forEachIndexed { index, parameter ->
//            val inputParameter = createInputParameter(index, parameter)
//            inputParams.add(inputParameter)
//        }
//        return this
//    }

    inline fun <reified INPUT: Any> captureInput(parameter: INPUT, block:INPUT.()-> Unit):DebugProxy<T,P>{
        inputParams.clear()
        val captureBlock = CaptureBlock(parameter)

        block.invoke(captureBlock.parameter)

        captureBlock.inputParams.forEach {
            this.inputParams.add(it)
        }
        return this
    }

//    inline fun <reified INPUT: Any> capture(parameter: INPUT, captureBlock:CaptureBlock<INPUT>.()-> Unit):DebugProxy<T,P>{
//        inputParams.clear()
//        captureInput(parameter)
//        val param = inputParams.first()
//        val block = CaptureBlock(parameter, param)
//        captureBlock.invoke(block)
//        block.inputParams.forEach {
//            param.addListParameter(it)
//        }
//        return this
//    }

    fun inputParameters(): List<InputParameter>{
        return inputParams
    }
}

fun <T: CTX, P: PrintableBase<P>> TasksManaged.debugProxy(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    usingTemplate: PrintableTemplate<P>? = null,
    dataProvider: (DebugParams<P>)-> P
):DebugProxy<T, P>{
    val dataProcessor = this.logHandler.dispatcher.getActiveDataProcessor()
    val proxy = DebugProxy(receiver,printableClass, dataProcessor,  dataProvider)
    proxy.activeTemplate = usingTemplate
    return  proxy
}