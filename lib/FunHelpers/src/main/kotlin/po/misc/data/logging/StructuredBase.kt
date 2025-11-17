package po.misc.data.logging

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.KeyPreset
import po.misc.data.pretty_print.PrettyCell
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.ValuePreset
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.time.TimeHelper
import java.time.Instant


abstract class StructuredBase(
    val loggable: Loggable,
):  StructuredLoggable, TimeHelper, PrettyPrint {
    override val context: TraceableContext = loggable.context
    override val subject: String = loggable.subject
    override val text: String = loggable.text
    override val topic: NotificationTopic = NotificationTopic.Info
    override val created: Instant = Instant.now()
    val logRecords: MutableList<Loggable> = mutableListOf()

    private val printedRow = PrettyRow(PrettyCell(10, KeyPreset), PrettyCell(10, ValuePreset), separator = SpecialChars.WHITESPACE)

    override val formattedString: String get() {
       return buildString {
            appendLine("${subject}[$topic]".colorize(Colour.CyanBright))
            appendLine(printedRow.render("Text", text))
        }
    }
    abstract fun track(context: TraceableContext, methodName: String)
    override fun addRecord(record: Loggable): Boolean = logRecords.add(record)
    override fun getRecords(): List<Loggable> = logRecords.toList()
    override fun toString(): String =
        buildString {
            appendLine(topic.name)
            appendLine("Subject: $subject")
            appendLine("Message: $text")
        }
}