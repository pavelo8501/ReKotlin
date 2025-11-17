package po.misc.data.logging.procedural

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.logging.Loggable
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.printable.grouping.printableProperty
import po.misc.data.badges.Badge
import po.misc.data.logging.log_subject.LogSubject
import po.misc.data.pretty_print.Console120
import po.misc.data.pretty_print.PrettyCell
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.types.token.TypeToken
import java.time.Instant


/**
 * A structured log record that represents a procedural or multi-step operation.
 *
 * This record extends the standard [Loggable] contract by introducing
 * a collection of [ProceduralEntry] steps that describe the detailed
 * execution flow of an operation — for example, reading configuration,
 * performing validation, or invoking nested resolvers.
 *
 * Each procedural step can record its own result and timing,
 * allowing the final record to summarize the entire operation outcome.
 *
 * Example:
 * ```kotlin
 * val record = ProceduralRecord(notification("Test Start"))
 * record.registerRecord(ProceduralEntry("Step_1", "[SYNC]"))
 * record.registerRecord(ProceduralEntry("Step_2", "[SYNC]"))
 * record.finalizeRecord()
 * record.output()
 * ```
 *
 * @property context the originating [TraceableContext] from which this record was emitted
 * @property topic the [NotificationTopic] describing the severity or intent of the record
 * @property subject the logical subject or label for this record (e.g., "Parsing Config")
 * @property text the human-readable message text for the record
 * @property created the timestamp marking when this record was created
 *
 * @see StructuredLoggable
 * @see ProceduralEntry
 * @see ProceduralFlow
 * @see po.misc.data.logging.processor.LogProcessor.logScope
 */

class ProceduralRecord(
    internal val logMessage: LogMessage
): PrintableBase<ProceduralRecord>(this), ProceduralData,  LoggableTemplate{

    constructor(context: TraceableContext,  logSubject: LogSubject):this(logSubject.toLogMessage(context))

    override val context: TraceableContext = logMessage.context
    override val subject: String = logMessage.subject
    override val text: String = logMessage.text
    override val created: Instant = logMessage.created

    override var topic: NotificationTopic = NotificationTopic.Info

    /**
     * Creates a new [ProceduralRecord] using an existing [Loggable] as its base.
     * Copies over the [context], [topic], [subject], [text], and [created]
     * fields from the provided loggable instance.
     */
    constructor(logRecord: Loggable): this(logRecord.toLogMessage())

    var result: ProceduralResult = ProceduralResult.Warning
    var indentSize: Int = 0

    override val self: ProceduralRecord = this
    val entries: MutableList<ProceduralEntry> = mutableListOf()

    @PublishedApi
    internal var resultPostfix: String = ""

    private val resultText: String = "Overall result ..........."

    val records: MutableList<StructuredLoggable> by printableProperty{loggable->
        val lastEntry = entries.lastOrNull()
        if(lastEntry != null){
            lastEntry.logRecords.add(loggable)
        }else{
            val entry = ProceduralFlow.createEntry(loggable)
            entry.logRecords.add(loggable)
            entries.add(entry)
        }
        logMessage.addRecord(loggable)
    }

    init {
        printableConfig.explicitOutput = true
    }

    fun extractMessage():LogMessage{
        entries.forEach {entry->
            val list = entry.extractMessage()
            list.forEach {loggable->
                if(logMessage.getRecords().none { it === loggable }){
                    logMessage.addRecord(loggable)
                }
            }
        }
        return logMessage
    }

    fun outputRecord(){
        echo(Start)
        entries.forEach {
            it.outputEntry()
        }
        echo(Result)
    }

    fun calculateResult(){
        if(entries.any { it.stepResult is StepResult.Fail }){
           result =  ProceduralResult.Fail
        }
        if(entries.all { it.stepResult is StepResult.OK }){
            result =  ProceduralResult.Ok
        }
    }

    fun registerRecord(record: ProceduralRecord){
        val lastEntry = entries.lastOrNull()
        if(lastEntry != null){
            lastEntry.addRecord(record)
        }else{
            val newEntry = ProceduralFlow.createEntry(record.subject, Badge.Init)
            newEntry.addRecord(record)
            entries.add(newEntry)
        }
    }
    fun registerEntry(entry: ProceduralData){
        when(entry){
            is ProceduralRecord -> registerRecord(entry)
            is ProceduralEntry -> entries.add(entry)
        }
    }
    override fun add(record: StructuredLoggable): Boolean = records.add(record)
    override fun get(): List<StructuredLoggable> =  records
    companion object: PrintableCompanion<ProceduralRecord>(TypeToken.create()){
        val Start: Template<ProceduralRecord> = createTemplate {
            nextLine {
                val name =  ClassResolver.instanceName(context)
                "[${name} @ ${created.hoursFormated(3)}] [$subject]".colorize(Colour.Blue).newLine {
                    text.colorize(Colour.BlackBright)
                }
            }
        }
        val Result: Template<ProceduralRecord> = createTemplate {
            nextLine {
                when(result){
                    ProceduralResult.Ok-> "$resultText ${"OK $resultPostfix".colorize(Colour.Green)}"
                    ProceduralResult.Warning -> "$resultText ${"Warning $resultPostfix".colorize(Colour.Yellow)}"
                    ProceduralResult.Fail -> "$resultText ${"Fail $resultPostfix".colorize(Colour.Red)}"
                }
            }
        }
    }
}

class ProceduralEntry(val stepBadge: Badge, val stepName: String) : ProceduralData, PrettyPrint {

    constructor(stepBadge: Badge, stepName: String, result: StepResult):this(stepBadge, stepName){
        stepResult = result
    }

    private val badgeCell = PrettyCell(width =  5)
    private val stepNameCell = PrettyCell.build(width = 20){
        emptySpaceFiller = SpecialChars.DOT
    }
    private val resultCell = PrettyCell(width = 2)
    private val outputRow = PrettyRow(badgeCell, stepNameCell, resultCell, separator = SpecialChars.WHITESPACE)

    internal val proceduralRecords: MutableList<ProceduralRecord> = mutableListOf()

    val created: Instant = Instant.now()
    var stepResult: StepResult? = null

    val result: Boolean get() = stepResult?.ok?:false
    val logRecords: MutableList<StructuredLoggable> = mutableListOf()

    override val formattedString: String get() {
        return outputRow.render(stepBadge.caption, stepName, stepResult?.formattedString?:"N/A")
    }

    fun extractMessage(): List<StructuredLoggable>{
        proceduralRecords.forEach {
            it.extractMessage()
        }
        return logRecords
    }

    fun add(data: StructuredLoggable):ProceduralEntry{
        logRecords.add(data)
        return  this
    }

    fun addRecord(entry: ProceduralRecord): ProceduralEntry{
        proceduralRecords.add(entry)
        logRecords.add(entry.logMessage)
        return this
    }

    fun outputEntry(){
        println(formattedString)
        proceduralRecords.forEach {
            it.outputRecord()
        }
    }
}

sealed interface ProceduralData

