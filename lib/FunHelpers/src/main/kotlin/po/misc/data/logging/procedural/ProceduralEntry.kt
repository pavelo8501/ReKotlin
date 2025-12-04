package po.misc.data.logging.procedural

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.badges.Badge
import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.IndentOptions
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken
import java.time.Instant



class ProceduralEntry(
    val stepBadge: Badge,
    val stepName: String,
    val parentRecord: LoggableTemplate
): ProceduralData, PrettyPrint, TraceableContext {

    constructor(
        stepBadge: Badge,
        stepName: String,
        result: StepResult,
        parentRecord: LoggableTemplate
    ):this(stepBadge, stepName, parentRecord){
        stepResult = result
    }

    constructor(
        record: StructuredLoggable,
        parentRecord: LoggableTemplate,
        result: StepResult? = null,
        stepBadge: Badge? = null,
    ):this(stepBadge?:defaultBadge, record.text, parentRecord){
        stepResult = result
        logRecords.add(record)
    }

    private val badgeCell = PrettyCell(width =  5)
    private val stepNameCell =  PrettyCell(20)

    private val resultCell = PrettyCell(width = 2)
    private val outputRow = PrettyRow(badgeCell, stepNameCell, resultCell)

    internal val proceduralRecords: MutableList<ProceduralRecord> = mutableListOf()

    val created: Instant = Instant.now()
    var stepResult: StepResult? = null

    val result: Boolean get() = stepResult?.ok?:false
    val logRecords: MutableList<StructuredLoggable> = mutableListOf()

    override val formattedString: String get() {
        return outputRow.render(stepBadge.caption, stepName, stepResult?.formattedString?:"N/A")
    }

    internal fun getStatistics(): String{
        "Entry logRecords = ${logRecords.size}"
        "Entry proceduralRecords = ${proceduralRecords.size}"
        return buildString {
            appendLine( "Entry logRecords = ${logRecords.size}")
            appendLine("Entry proceduralRecords = ${proceduralRecords.size}")
            proceduralRecords.forEach {
                appendLine(it.getStatistics())
            }
        }
    }

    fun extractMessage(): List<StructuredLoggable>{
        proceduralRecords.forEach {
            it.extractMessage()
        }
        return logRecords
    }

    fun addRecord(data: StructuredLoggable):ProceduralEntry{
        logRecords.add(data)
        return  this
    }

    fun addEntry(entry: LoggableTemplate): ProceduralEntry{
        when(entry){
            is ProceduralRecord-> {
                proceduralRecords.add(entry)
                logRecords.add(entry.logRecord)
            }
            else -> {
                logRecords.add(entry.logRecord)
            }
        }
        return this
    }

    fun outputEntry(indentionLevel: Int){
        // println(formattedString)
        formattedString.output(IndentOptions(indentionLevel, " "))
        proceduralRecords.forEach {
            it.outputRecord(indentionLevel + 2)
        }
    }
    override fun toString(): String =
        "ProceduralEntry ${parentRecord.orDefault{ "on $it" }} [Procedural count: ${proceduralRecords.size} LogRecords count ${logRecords.size}]"

    companion object{
        val defaultBadge : Badge = Badge.Init
    }
}
