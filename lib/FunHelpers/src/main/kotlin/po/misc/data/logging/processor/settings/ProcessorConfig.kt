package po.misc.data.logging.processor.settings

import po.misc.data.PrettyPrint
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.reflection.NameValuePair
import po.misc.reflection.nameValuePair
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


data class StateSnapshot(
    private val processor: LogProcessor<*, *>,
    val name : String = processor.name,
    val loggedMessageCount: Int = processor.logRecords.size,
    val shouldStoreRecords : Boolean = processor.shouldStoreRecords,
    val generalMute : Boolean  = processor.generalMute,
    val hasActiveRecord : Boolean = processor.activeRecord != null,
    val hasActiveUnresolved : Boolean = processor.activeUnresolved != null,
    val activeDataHandlers: List<KClass<*>>  = processor.logForwarder.handlerRegistrations.map { it.baseClassHandled }
): PrettyPrint{


    private val keySlot = PrettyCell(PrettyPresets.Key, 10)
    private val valueSlot = PrettyCell(PrettyPresets.Value, 10)

    private val prettyRow = PrettyRow(keySlot, valueSlot)

    override val formattedString: String
        get() =  buildString {
            append(prettyRow.render(::name.nameValuePair))
            appendLine(prettyRow.render(::name.nameValuePair))
            appendLine(prettyRow.render(::loggedMessageCount.nameValuePair))
            appendLine(prettyRow.render(::shouldStoreRecords.nameValuePair))
            appendLine(prettyRow.render(::hasActiveRecord.nameValuePair))
            appendLine(prettyRow.render(::hasActiveUnresolved.nameValuePair))
            appendLine(prettyRow.render(::activeDataHandlers.nameValuePair))
    }

}


data class ProcessorConfig(
    var notifyOverwrites: Boolean = true,
    var notifyDataUnhandled: Boolean = true,
    var maxHandlerHierarchyDepth: Int = 4
) {
}