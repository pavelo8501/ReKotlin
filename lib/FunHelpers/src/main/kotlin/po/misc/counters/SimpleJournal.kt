package po.misc.counters

import po.misc.data.PrettyPrint
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.throwableToText

class SimpleJournal(
    var journalName :String,
) : PrettyPrint {

    val errors: MutableList<Throwable> get() = mutableListOf()
    internal val messagesBacking: MutableList<DataRecord> = mutableListOf()
    val records:List<DataRecord> = messagesBacking

    private val formattedRecords get() = records.joinToString(separator = SpecialChars.NEW_LINE) { it.formattedString  }

    override val formattedString: String get() = "$journalName${SpecialChars.NEW_LINE}${formattedRecords}"

    fun add(record: DataRecord):SimpleJournal {
        messagesBacking.add(record)
        return this
    }

    fun record(message: String, type: DataRecord.MessageType):SimpleJournal = add(DataRecord(message, type))

    fun info(message: String):SimpleJournal = add(DataRecord(message, DataRecord.MessageType.Info))
    fun success(message: String):SimpleJournal =  add(DataRecord(message, DataRecord.MessageType.Success))
    fun warning(message: String):SimpleJournal =  add(DataRecord(message, DataRecord.MessageType.Warning))
    fun register(th : Throwable): SimpleJournal{
        messagesBacking.add(DataRecord(th.throwableToText(), DataRecord.MessageType.Failure))
        errors.add(th)
        return this
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

    override fun toString(): String = journalName
}


