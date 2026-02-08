package po.misc.data.logging.procedural

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.Topic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.badges.Badge
import po.misc.data.logging.processor.contracts.TemplateActions
import po.misc.data.logging.processor.parts.StructuredOptions
import po.misc.data.logging.processor.parts.structuredProperty
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
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
 * @property topic the [Topic] describing the severity or intent of the record
 * @property subject the logical subject or label for this record (e.g., "Parsing Config")
 * @property text the human-readable message text for the record
 * @property created the timestamp marking when this record was created
 *
 * @see StructuredLoggable
 * @see ProceduralEntry
 * @see ProceduralFlow
 */

class ProceduralRecord(
    override val logRecord: LogMessage,
    private val topNode: Boolean = true
): PrintableBase<ProceduralRecord>(this), ProceduralData,  LoggableTemplate{

    enum class ProceduralTemplate: RowID { Record, Entry,  }

    constructor(logRecord: StructuredLoggable, topNode: Boolean = true): this(logRecord.toLogMessage(), topNode)

    override val context: TraceableContext = logRecord.context
    override val subject: String = logRecord.subject
    override val text: String = logRecord.text
    override val created: Instant = logRecord.created
    override val topic: Topic = logRecord.topic

    private var currentIndentLevel: Int = 0


    val result: ProceduralResult get() {
       return calculateResult()
    }

    override val self: ProceduralRecord = this
    val proceduralEntries: MutableList<ProceduralEntry> = mutableListOf()

    @PublishedApi
    internal var resultPostfix: String = ""

    private val resultText: String get() {
       return if(topNode){
            "Overall result ..........."
        }else{
            "$subject result ..........."
        }
    }

    internal val structuredOptions = StructuredOptions(this){loggable->
        val lastEntry = proceduralEntries.lastOrNull()
        if(lastEntry != null){
            lastEntry.addRecord(loggable)
        }else{
            val entry = ProceduralFlow.createEntry(loggable)
            entry.logRecords.add(loggable)
            proceduralEntries.add(entry)
        }
        logRecord.addRecord(loggable)
    }
    val logRecords: MutableList<StructuredLoggable> by structuredProperty(structuredOptions)


//    val logRecords: MutableList<StructuredLoggable> by printableProperty{loggable->
//
//        val lastEntry = proceduralEntries.lastOrNull()
//        if(lastEntry != null){
//            lastEntry.addRecord(loggable)
//        }else{
//            val entry = ProceduralFlow.createEntry(this@ProceduralRecord, loggable)
//            entry.logRecords.add(loggable)
//            proceduralEntries.add(entry)
//        }
//        logRecord.addRecord(loggable)
//    }


    val proceduralRecords : List<ProceduralRecord> get() = getRecords().filterIsInstance<ProceduralRecord>()

    init {
        printableConfig.explicitOutput = true
    }

    internal fun getStatistics(): String{
       val result = buildString {
            appendLine( "Top entries = ${proceduralEntries.size}")
            appendLine( "Top messages = ${logRecords.size}")
           proceduralEntries.forEach {
                it.getStatistics()
            }
        }
       return result
    }

    fun extractMessage():LogMessage{
        proceduralEntries.forEach {entry->
            val list = entry.extractMessage()
            list.forEach {loggable->
                if(logRecord.getRecords().none { it === loggable }){
                    logRecord.addRecord(loggable)
                }
            }
        }
        return logRecord
    }

    fun outputRecord(indentionLevel: Int = 0){
        currentIndentLevel = indentionLevel
        echo(Start)
        proceduralEntries.forEach {
            it.outputEntry(indentionLevel)
        }
        echo(Result)
    }

    private fun getSubResult():ProceduralResult{
        if(proceduralRecords.any { it.result == ProceduralResult.Fail }){
            return ProceduralResult.Fail
        }
        if(proceduralRecords.any { it.result == ProceduralResult.Warning }){
            return ProceduralResult.Warning
        }
        return ProceduralResult.Ok
    }

    fun calculateResult(): ProceduralResult {
        proceduralEntries.flatMap { it.records }.forEach {
            it.calculateResult()
        }
        if(proceduralEntries.any { it.stepResult is StepResult.Fail }){
           return ProceduralResult.Fail
        }
        if(proceduralEntries.any { it.stepResult is StepResult.Warning }){
            return ProceduralResult.Warning
        }

        val subResult = getSubResult()
        if(subResult != ProceduralResult.Ok){
            return subResult
        }
        return ProceduralResult.Ok
    }
    override fun addRecord(templateRecord: LoggableTemplate){
        val lastEntry = proceduralEntries.lastOrNull()
        if(lastEntry != null){
            logRecord.addRecord(templateRecord.logRecord)
            lastEntry.addEntry(templateRecord)
        }else{
            val newEntry = ProceduralFlow.createEntry(templateRecord.subject, Badge.Init)
            newEntry.logRecords.add(templateRecord.logRecord)
            newEntry.addEntry(templateRecord)
            proceduralEntries.add(newEntry)
        }
    }
    override fun getRecords(): List<LoggableTemplate>{
        val result =  proceduralEntries.flatMap { it.records }
        return result
    }
    override fun getRecord(action : TemplateActions):LoggableTemplate{
        return when (action) {
            is LastRegistered -> {
                val lastProcedural = proceduralRecords.lastOrNull()
                 lastProcedural?.getRecord(action) ?: this
            }
        }
    }
    override fun addEntry(entry: ProceduralEntry){
        proceduralEntries.add(entry)
    }

    fun registerRecord(record: ProceduralRecord): Unit = addRecord(record)

    fun registerEntry(entry: ProceduralData){
        when(entry){
            is ProceduralRecord ->  addRecord(entry)
            is ProceduralEntry -> addEntry(entry)
        }
    }
    override fun addMessage(record: StructuredLoggable): Boolean = logRecords.add(record)
    override fun getMessages(): List<StructuredLoggable> =  logRecords

    override fun toString(): String = "ProceduralRecord [Source message: ${text}]"

    companion object: PrintableCompanion<ProceduralRecord>(TypeToken.create()){


        val Start: Template<ProceduralRecord> = createTemplate {
            nextLine {
                val name =  ClassResolver.instanceName(context)
                val resultingText = "[${name} @ ${created.hoursFormated(3)}] [$subject]".colorize(Colour.Blue).newLine {
                    text.stringify().toString()
                }
                resultingText.stringify().toString()
            }
        }
        val Result: Template<ProceduralRecord> = createTemplate {
            nextLine {
                val resultingText = when(result){
                    ProceduralResult.Ok-> "$resultText ${"OK $resultPostfix".colorize(Colour.Green)}"
                    ProceduralResult.Warning -> "$resultText ${"Warning $resultPostfix".colorize(Colour.Yellow)}"
                    ProceduralResult.Fail -> "$resultText ${"Fail $resultPostfix".colorize(Colour.Red)}"
                }
                resultingText.stringify().toString()
            }
        }
    }
}


