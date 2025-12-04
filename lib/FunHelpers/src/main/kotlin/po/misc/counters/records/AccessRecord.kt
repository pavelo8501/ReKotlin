package po.misc.counters.records

import po.misc.counters.JournalBase
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.formatters.text_modifiers.ColorModifier
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.time.TimeHelper
import java.time.Instant

data class AccessRecord <E: Enum<E>>(
    override val message: String,
    val journal : JournalBase<E>,
): JournalEntry<E>, PrettyPrint, TimeHelper, Templated {

    val hostName : String get() =  journal.hostInstanceInfo.instanceName

    val accessTime: Instant = Instant.now()
    val formatedTime: String = accessTime.toLocalTime()


    private var recordSuccess: Boolean = false

    private val noKeyOption = KeyedCellOptions(showKey = false)
    private val success = ColorModifier.ColourCondition("Success", Colour.GreenBright)
    private val failure = ColorModifier.ColourCondition("Failure", Colour.RedBright)
    private val dynamicCondition = ColorModifier {
        if (recordSuccess) Colour.GreenBright
        else Colour.RedBright
    }

    private val prettyRow = buildPrettyRow<AccessRecord<*>>(CellContainer.Companion) {
        addCell(::formatedTime, KeyedCellOptions(showKey = false, width = 20))
        addCell(::entryType, noKeyOption, ColorModifier(success, failure))
        addCell(::message)
        addCell(::hostName)
        addCell(::reasoning, KeyedCellOptions(showKey = false), dynamicCondition)
    }

    override val formattedString: String get() = prettyRow.render(this)

    var reasoning: String = ""
        private set

    override var entryType: E = journal.defaultRecordType
        internal set

    var active: Boolean = true
        private set


    fun changeRecordType(type: E): AccessRecord<E>{
        entryType = type
        return this
    }

    override fun resultOK(successType: E, message: String?){
        entryType = successType
        active = false
        recordSuccess = true
        message?.let {
            reasoning = it
        }
    }

    override fun resultFailure(failureType: E, reason: String){
        entryType = failureType
        active = false
        recordSuccess = false
        reasoning = reason
    }
}