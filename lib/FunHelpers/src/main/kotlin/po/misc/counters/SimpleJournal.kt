package po.misc.counters

import po.misc.data.PrettyPrint
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.throwableToText

class SimpleJournal(
    private var hostName :String
) : PrettyPrint {


    val journalName :String = "Journal of $hostName"

    val errors: MutableList<Throwable> get() = mutableListOf()
    internal val messagesBacking: MutableList<DataRecord> = mutableListOf()
    val records:List<DataRecord> = messagesBacking

    private val formattedRecords get() = records.joinToString(separator = SpecialChars.NEW_LINE) { it.formattedString  }

    override val formattedString: String get() = "$journalName${SpecialChars.NEW_LINE}${formattedRecords}"

    fun add(record: DataRecord):DataRecord {
        messagesBacking.add(record)
        return record
    }

    fun record(message: String, type: DataRecord.MessageType):DataRecord = add(DataRecord(message, type))

    fun info(message: String):DataRecord = add(DataRecord(message, DataRecord.MessageType.Info))
    fun success(message: String):DataRecord =  add(DataRecord(message, DataRecord.MessageType.Success))
    fun warning(message: String):DataRecord =  add(DataRecord(message, DataRecord.MessageType.Warning))
    fun register(th : Throwable): DataRecord{
        val record = DataRecord(th.throwableToText(), DataRecord.MessageType.Failure)
        messagesBacking.add(record)
        errors.add(th)
        return record
    }

    fun submit(result: Boolean, message: String? = null): SimpleJournal{
        if(message != null){
            if(result){
               success(message)
            }else{
                record(message, DataRecord.MessageType.Failure)
            }
        }
        return this
    }

    fun updateName(name:String):SimpleJournal{
        hostName = name
        return this
    }

    override fun toString(): String = journalName
}


