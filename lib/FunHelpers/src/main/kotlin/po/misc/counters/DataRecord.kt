package po.misc.counters

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.styles.Colour
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import java.time.Instant


data class MethodRec(
    val methodName:String,
    val description:String = "",
    var tracker: ((DataRecord)-> Unit)? = null
){
    val start: Instant = Instant.now()
    val records = mutableListOf<DataRecord>()


    fun add(record: DataRecord, trackerAction: (DataRecord)-> Unit):MethodRec{
        trackerAction.invoke(record)
        tracker =  trackerAction
        records.add(record)
        return this
    }

    fun add(record: DataRecord):MethodRec{
        tracker?.invoke(record)
        records.add(record)
        return this
    }
}

class DataRecord(
    val message: String,
    val recordType: MessageType,

): PrettyPrint {

    enum class MessageType { Info, Success, Failure,  Warning }
    private val commentsBacking = mutableListOf<DataRecord>()

        // private val commentsBacking = mutableListOf<String>()
    val comments: List<DataRecord> = commentsBacking

    val placeholders: MutableList<Any> = mutableListOf()
    val hasPlaceholders:Boolean get() =  placeholders.isNotEmpty()


    override val formattedString: String get(){
       return "[$recordType] $message"
    }
    var trackedMethod: MethodRec? = null
    fun addTracking(methodRec: MethodRec):DataRecord {
        trackedMethod = methodRec
        return this
    }

    fun addPlaceholder(placeholder: Any):DataRecord {
        placeholders.add(placeholder)
        return this
    }

    override fun toString(): String = "[${recordType.name}] $message"

    fun warn(vararg  text:String):DataRecord {
        val data = DataRecord(text.joinToString(),  MessageType.Warning)
        trackedMethod?.add(data) ?:run {
            commentsBacking.add(data)
        }
        return this
    }

    fun addComment(text:String):DataRecord {
        val data = DataRecord(text, recordType)
        trackedMethod?.add(data) ?:run {
            commentsBacking.add(data)
        }
        return this
    }

    companion object {

//        override val type: TypeToken<DataRecord> = tokenOf()
//        val template: PrettyGrid<DataRecord> = buildPrettyGrid{
//            buildRow {
//                computed(DataRecord::recordType) {
//                    colourConditions {
//                        Colour.Blue.buildCondition { contains(MessageType.Info.name) }
//                        Colour.GreenBright.buildCondition { contains(MessageType.Success.name) }
//                        Colour.Red.buildCondition { contains(MessageType.Failure.name) }
//                        Colour.YellowBright.buildCondition { contains(MessageType.Warning.name) }
//                    }
//                }
//                add(DataRecord::message, CellPresets.KeylessProperty)
//            }
//        }
    }
}