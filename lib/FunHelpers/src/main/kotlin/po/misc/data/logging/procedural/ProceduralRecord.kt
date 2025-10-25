package po.misc.data.logging.procedural

import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.printable.grouping.printableProperty
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
    override val context: TraceableContext,
    override val topic: NotificationTopic,
    override val subject: String,
    override val text: String,
    override val created: Instant
): PrintableBase<ProceduralRecord>(this), StructuredLoggable, TraceableContext{

    /**
     * Creates a new [ProceduralRecord] using an existing [Loggable] as its base.
     * Copies over the [context], [topic], [subject], [text], and [created]
     * fields from the provided loggable instance.
     */
    constructor(logRecord: Loggable):this(logRecord.context, logRecord.topic, logRecord.subject, logRecord.text, logRecord.created)

    private val overallResult: String get() {
       val isFail =  procedural.any {
            it.stepResult ==  StepResult.Fail
       }
     return  if(isFail){
           "FAIL".colorize(Colour.Red)
       }else{
           "OK".colorize(Colour.Green)
       }
    }
    override val self: ProceduralRecord = this
    val procedural: MutableList<ProceduralEntry> by printableProperty<ProceduralEntry>{procedural->
        procedural.onComplete {
            procedural.output()
            procedural.complete = null
        }
    }
    override val formattedString: String get() = super.formattedString

    init {
        setDefaultTemplate(Start)
        printableConfig.explicitOutput = false
    }

    fun finalizeRecord(){
        setDefaultTemplate(Result)
    }
    override fun registerRecord(record: ProceduralEntry){
        procedural.add(record)
    }

    companion object: PrintableCompanion<ProceduralRecord>(TypeToken.create()){
        val Start: Template<ProceduralRecord> = createTemplate {
            nextLine {
                val name =  ClassResolver.instanceName(context)
                "[${name} @ ${created.hoursFormated(3)}] [$subject]".colorize(Colour.Blue).newLine {
                    "$text .......... ".colorize(Colour.WhiteBright)
                }
            }
        }

        val Result: Template<ProceduralRecord> = createTemplate {
            nextLine {
                "Overall result ........... $overallResult"
            }
        }
    }
}
