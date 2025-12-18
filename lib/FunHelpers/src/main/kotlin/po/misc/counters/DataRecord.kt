package po.misc.counters

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour

class DataRecord(
    val message: String,
    val recordType: MessageType,
): PrettyPrint {

    enum class MessageType { Info, Success, Failure,  Warning }

    override val formattedString: String get() = template.render(this)

    override fun toString(): String = "[${recordType.name}] $message"

    companion object {

        val template: PrettyRow<DataRecord> = buildPrettyRow{
            computed(DataRecord::recordType) {
                colourConditions {
                    Colour.Blue.buildCondition { contains(MessageType.Info.name) }
                    Colour.GreenBright.buildCondition { contains(MessageType.Success.name) }
                    Colour.Red.buildCondition { contains(MessageType.Failure.name) }
                    Colour.YellowBright.buildCondition { contains(MessageType.Warning.name) }
                }
            }
            add(DataRecord::message, CellPresets.KeylessProperty)
        }

    }
}