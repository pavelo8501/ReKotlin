package po.misc.counters

import po.misc.counters.records.LogJournalEntry
import po.misc.data.PrettyPrint
import po.misc.data.helpers.orDefault
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.throwableToText
import po.misc.types.k_class.simpleOrAnon
import java.time.Instant
import kotlin.reflect.KClass

class SimpleJournal(
    var hostName :String,
    var verbosity: Verbosity = Verbosity.Warnings
): PrettyPrint {

    constructor(
        hostClass: KClass<*>,
        verbosity: Verbosity = Verbosity.Warnings
    ):this(hostClass.simpleOrAnon, verbosity)

    val journalName :String = "Journal of $hostName"
    val errors: MutableList<Throwable> get() = mutableListOf()
    internal val messagesBacking: MutableList<DataRecord> = mutableListOf()
    val records:List<DataRecord> = messagesBacking

    private val formattedRecords get() = records.joinToString(separator = SpecialChars.NEW_LINE) { it.formattedString  }
    override val formattedString: String get() = "$journalName${SpecialChars.NEW_LINE}${formattedRecords}"

    private fun outputRecord(record: DataRecord){
        val prefix = "$journalName -> "
        println("$prefix${record.formattedString}")
    }


    fun add(record: DataRecord):DataRecord {
        messagesBacking.add(record)
        val isFailOrWarn = record.recordType ==  DataRecord.MessageType.Failure || record.recordType ==  DataRecord.MessageType.Warning
        if(isFailOrWarn || verbosity == Verbosity.Debug){
            outputRecord(record)
        }
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

    data class ParamRec(
        val name:String,
        val value: Any?
    ){
        override fun toString(): String = "$name:$value"
    }

    fun method(methodName: String, vararg initialParameters: ParamRec):DataRecord{
        val parameterString = initialParameters.map { it.toString() }.joinToString(", ")
        val msg = "Method: $methodName Parameters: ${parameterString}"
        MethodRec(methodName)
        val data =  DataRecord(msg, DataRecord.MessageType.Info)
        return  add(data)
    }

    fun method(methodName: String, verbosity: Verbosity):DataRecord{
        this.verbosity = verbosity
        val methodRec = MethodRec(methodName){
            outputRecord(it)
        }
        val data = DataRecord("Method: $methodName}", DataRecord.MessageType.Info).addTracking(methodRec)
        return  add(data)
    }

    fun method(methodName: String, description: String = ""):DataRecord{
        val methodRec =  MethodRec(methodName, description)
        val msg = "Method: $methodName${description.orDefault { "Description: $it" }}"
        val data = DataRecord(msg, DataRecord.MessageType.Info).addPlaceholder(methodRec)
        return  add(data)
    }

    fun methodNext(methodName: String, description: String = ""):DataRecord?{
       return records.lastOrNull { it.hasPlaceholders }?.addPlaceholder(MethodRec(methodName, description))
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


