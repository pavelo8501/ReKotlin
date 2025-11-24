package po.misc.data.logging.processor.parts

import po.misc.collections.reactive_list.ReactiveList
import po.misc.collections.reactive_list.ReactiveList.Options
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.processor.contracts.ProceduralContract



class StructuredOptions(
    val template: LoggableTemplate,
    val contract: ProceduralContract,
    var disableSideEffects: Boolean = false,
    val options: Options? = null,
    val onEntry: (StructuredLoggable)-> Unit,
){

    constructor(template: LoggableTemplate, onEntry: (StructuredLoggable)-> Unit):this(
        template,
        ProceduralContract(template),
        onEntry = onEntry
    )

    init {
        options?.let {
            it.disableSideEffects = disableSideEffects
        }
    }

}

open class StructuredList(
    val structuredOptions: StructuredOptions,
) : ReactiveList<StructuredLoggable, Unit>(structuredOptions.options, structuredOptions.onEntry) {

    val contract: ProceduralContract get() = structuredOptions.contract

    override fun newEntry(data: StructuredLoggable, noSideEffects: Boolean){
        contract.addRecord(data)
        if(!structuredOptions.disableSideEffects){
            structuredOptions.onEntry.invoke(data)
        }
    }

}