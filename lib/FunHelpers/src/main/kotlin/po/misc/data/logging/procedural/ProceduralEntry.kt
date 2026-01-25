package po.misc.data.logging.procedural

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.badges.Badge
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.strings.appendGroup
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import java.time.Instant


class ProceduralEntry(
    val badge: Badge,
    val stepName: String,
) : ProceduralData, PrettyPrint, TraceableContext {

    constructor(
        stepBadge: Badge,
        stepName: String,
        result: StepResult,
    ):this(stepBadge, stepName){
        stepResult = result
    }

    constructor(
        record: StructuredLoggable,
        result: StepResult? = null,
        stepBadge: Badge? = null,
    ):this(stepBadge?:defaultBadge, record.text){
        stepResult = result
        logRecords.add(record)
    }

    private val badgeCell = PrettyCell(width =  5)
    private val stepNameCell =  PrettyCell(20)
    private val resultCell = PrettyCell(width = 2)
    private val outputRow = PrettyRow(badgeCell, stepNameCell, resultCell)

    internal val records: MutableList<ProceduralRecord> = mutableListOf()

    val nestedRecordsSize: Int get() = records.size

    val created: Instant = Instant.now()

    val result: Boolean get() = stepResult?.ok?:false
    val logRecords: MutableList<StructuredLoggable> = mutableListOf()
    private val warnings get() = logRecords.filter { it.topic == NotificationTopic.Warning }

    var stepResult: StepResult? = null
        get() {
            if(warnings.isNotEmpty()){
                return StepResult.Warning(warnings)
            }
            return field
        }

    override val formattedString: String get() {
        return outputRow.renderAny(badge.caption, stepName, stepResult?.formattedString?:"N/A")
    }

    internal fun getStatistics(): String{
        "Entry logRecords = ${logRecords.size}"
        "Entry proceduralRecords = ${records.size}"
        return buildString {
            appendLine( "Entry logRecords = ${logRecords.size}")
            appendLine("Entry proceduralRecords = ${records.size}")
            records.forEach {
                appendLine(it.getStatistics())
            }
        }
    }

    fun setResult(result: StepResult){
        stepResult = result
    }

    fun extractMessage(): List<StructuredLoggable>{
        records.forEach {
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
                records.add(entry)
                logRecords.add(entry.logRecord)
            }
            else -> {
                logRecords.add(entry.logRecord)
            }
        }
        return this
    }

    fun outputEntry(indentionLevel: Int){
        formattedString.output()
        records.forEach {
            it.outputRecord(indentionLevel + 2)
        }
    }

    override fun toString(): String = buildString {
        appendGroup("ProceduralEntry[", "]", ::nestedRecordsSize, ::created)
    }

    companion object : Templated<ProceduralEntry>{
        val defaultBadge : Badge = Badge.Init
        override val receiverType: TypeToken<ProceduralEntry> = tokenOf<ProceduralEntry>()
        private val entryOptions = buildRowOption(){
            renderBorders = false
            useId(ProceduralRecord.ProceduralTemplate.Entry)
        }
        private val resultOption = buildOption(){
            sourceFormat = true
            keyText = "Result :"
        }

        val template: PrettyGrid<ProceduralEntry> = buildGrid  {
            buildRow {
                add(ProceduralEntry::badge, CellPresets.KeylessProperty)
                add(ProceduralEntry::stepName){
                    applyOptions(CellPresets.KeylessProperty)
                }
                add(ProceduralEntry::stepResult, resultOption)
            }
            buildListGrid(ProceduralEntry::logRecords){
                buildRow {
                    applyOptions(RowPresets.BulletList)
                    add(StructuredLoggable::text){
                        it.colorize(Colour.YellowBright)
                    }
                }
            }
        }
    }
}
