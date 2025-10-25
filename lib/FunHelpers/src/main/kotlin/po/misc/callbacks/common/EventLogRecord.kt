package po.misc.callbacks.common

import po.misc.context.component.Component
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.nextLine
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken
import java.time.Instant


class EventLogRecord(
    override val context: Component,
    override val topic: NotificationTopic,
    override val subject: String,
    override val text: String
): PrintableBase<EventLogRecord>(this), Loggable, StructuredLoggable {

    override val created: Instant = Instant.now()

    override val self: EventLogRecord = this
    val records: MutableList<ProceduralEntry> = mutableListOf()

    init {
        setDefaultTemplate(ProceduralTemplate)
    }

    override fun registerRecord(record: ProceduralEntry) {
        records.add(record)
    }

    companion object: PrintableCompanion<EventLogRecord>(TypeToken.create()){

       internal val ProceduralTemplate = createTemplate {
            nextLine {
                context.componentID.componentName + " - > $subject"
            }
            nextLine {
                records.joinToString(separator = SpecialChars.NEW_LINE) {
                    it.formattedString
                }
            }
        }
    }
}