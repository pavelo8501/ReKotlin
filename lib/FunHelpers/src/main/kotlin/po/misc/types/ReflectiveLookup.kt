package po.misc.types

import po.misc.counters.DataRecord
import po.misc.data.PrettyPrint
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.throwableToText


class ReflectiveLookup(
    val lookupType: Type = Type.PropertyReturnType,
): PrettyPrint{

    enum class Type { PropertyReturnType  }

    var result: Boolean = true
        internal set

    val hasFailures: Boolean get() = errors.isNotEmpty()

    private val lookupName =  "ReflectiveLookup ${lookupType.name} [Result: $result]"

    val messages: MutableList<DataRecord> = mutableListOf()

    internal val errorsBacking: MutableList<Throwable> = mutableListOf()

    val errors: MutableList<Throwable> get() = errorsBacking

    override val formattedString: String get() =
        "$lookupName${SpecialChars.NEW_LINE}${messages.joinToString(separator = SpecialChars.NEW_LINE)}"

    fun message(message: String, type:  DataRecord.MessageType):ReflectiveLookup{
        messages.add(DataRecord(message, type))
        return this
    }
    fun success(message: String):ReflectiveLookup{
        messages.add(DataRecord(message, DataRecord.MessageType.Success))
        return this
    }
    fun warning(message: String):ReflectiveLookup{
        messages.add(DataRecord(message, DataRecord.MessageType.Warning))
        return this
    }
    fun info(message: String):ReflectiveLookup{
        messages.add(DataRecord(message, DataRecord.MessageType.Info))
        return this
    }

    fun registerThrowable(th : Throwable): ReflectiveLookup{
        result = false
        message(th.throwableToText(), DataRecord.MessageType.Failure)
        errorsBacking.add(th)
        return this
    }

    fun submitResult(result: Boolean, message: String? = null): ReflectiveLookup{
        this.result = result
        if(message != null){
            if(result){
                success(message)
            }else{
                message(message, DataRecord.MessageType.Failure)
            }
        }
        return this
    }

    override fun toString(): String = lookupName

}