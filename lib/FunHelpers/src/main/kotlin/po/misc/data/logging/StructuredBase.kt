package po.misc.data.logging

import po.misc.context.tracable.TraceableContext
import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
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
    
    private val colorizedText get() = when (topic) {
        NotificationTopic.Info, NotificationTopic.Debug -> " -> $text".colorize(Colour.WhiteBright)
        NotificationTopic.Warning -> " -> $text".colorize(Colour.YellowBright)
        NotificationTopic.Exception -> " -> $text".colorize(Colour.RedBright)
    }

    private val contextName get() =  ClassResolver.instanceName(context)

    override val formattedString: String get() =
        "$contextName @ ${created.toLocalTime()} [${subject}]".colorize(Colour.Blue).concat {
            colorizedText
        }

    abstract fun track(context: TraceableContext, methodName: String)
    override fun addRecord(record: Loggable): Boolean = logRecords.add(record)
    override fun getRecords(): List<Loggable> = logRecords.toList()
    override fun toString(): String =
        buildString {
            appendLine("Message<${topic.name}> $subject")
            appendLine(text)
            appendLine("By: $context")
        }
}