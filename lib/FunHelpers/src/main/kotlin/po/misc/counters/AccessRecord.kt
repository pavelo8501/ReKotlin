package po.misc.counters

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.cells.KeyedCellOptions
import po.misc.data.pretty_print.formatters.ColorModifier
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.time.TimeHelper
import java.time.Instant

data class AccessRecord <E: Enum<E>>(
    val journal : AccessJournal<E>,
    val message: String,
): PrettyPrint, TimeHelper {

    val hostName : String get() =  journal.hostInstanceInfo.instanceName

    val accessTime: Instant = Instant.now()
    val formatedTime: String = accessTime.toLocalTime()


    private var recordSuccess: Boolean = false

    private val noKeyOption = KeyedCellOptions(showKey = false)
    private val success = ColorModifier.ColourCondition("Success", Colour.GreenBright)
    private val failure = ColorModifier.ColourCondition("Failure", Colour.RedBright)
    private val dynamicCondition = ColorModifier{
        if(recordSuccess)Colour.GreenBright
        else Colour.RedBright
    }

    private val prettyRow = buildPrettyRow {
        addCell(::formatedTime, KeyedCellOptions(showKey = false, width = 20))
        addCell(::recordType, noKeyOption, ColorModifier(success, failure))
        addCell(::message)
        addCell(::hostName)
        addCell(::reasoning, KeyedCellOptions(showKey = false),  dynamicCondition)
    }

    override val formattedString: String get() = prettyRow.render(this)

    var reasoning: String = ""
        private set

    var recordType: E = journal.defaultRecordType
        private set

    var active: Boolean = true
        private set


    fun changeRecordType(type: E): AccessRecord<E>{
        recordType = type
        return this
    }

    fun resultOK(successType: E,  message: String? = null){
        recordType = successType
        active = false
        recordSuccess = true
        message?.let {
            reasoning = it
        }
    }

    fun resultFailure(failureType: E,  reason: String){
        recordType = failureType
        active = false
        recordSuccess = false
        reasoning = reason
    }
}