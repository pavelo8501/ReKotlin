package po.misc.counters

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.buildRow
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf

class DataRecord(
    val message: String,
    val recordType: MessageType,
): PrettyPrint {

    enum class MessageType { Info, Success, Failure,  Warning }
    override val formattedString: String get() = template.render(this)

    private val commentsBacking = mutableListOf<String>()

    val comments: List<String> = commentsBacking
    
    override fun toString(): String = "[${recordType.name}] $message"

    fun addComment(text:String):DataRecord{
        commentsBacking.add(text)
        return this
    }

    companion object : Templated<DataRecord> {

        override val valueType: TypeToken<DataRecord> = tokenOf()

        val template: PrettyGrid<DataRecord> = buildGrid{
            buildRow {
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

//        val template: PrettyRow<DataRecord> = buildPrettyRow{
//            computed(DataRecord::recordType) {
//                colourConditions {
//                    Colour.Blue.buildCondition { contains(MessageType.Info.name) }
//                    Colour.GreenBright.buildCondition { contains(MessageType.Success.name) }
//                    Colour.Red.buildCondition { contains(MessageType.Failure.name) }
//                    Colour.YellowBright.buildCondition { contains(MessageType.Warning.name) }
//                }
//            }
//            add(DataRecord::message, CellPresets.KeylessProperty)
//        }

    }
}