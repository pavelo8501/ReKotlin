package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.parts.AccessJournalDefaults
import po.misc.data.PrettyPrint
import po.misc.data.styles.SpecialChars
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo
import po.misc.functions.Throwing


abstract class JournalBase<E: Enum<E>>(
    val hostInstanceInfo: InstanceInfo,
    val defaultRecordType: E,
    var recordDefaults: AccessJournalDefaults<E>? = null
) : PrettyPrint {


    internal val journalRecords = mutableListOf<AccessRecord<E>>()

    val size : Int get() = journalRecords.size
    override val formattedString: String get() = journalRecords.joinToString(separator = SpecialChars.NEW_LINE) { it.formattedString }



    private fun registerRecord(message: String, recordType: E?): AccessRecord<E>{
        val accessRecord = AccessRecord<E>(this, message)
        recordType?.let {
            accessRecord.changeRecordType(it)
        }
        val orphan = journalRecords.filter { it.recordType == defaultRecordType}
        val failRec = recordDefaults?.failureRecordType
        if(failRec != null){
            orphan.forEach {
                it.resultFailure(failRec,  message + "before this record was finalized.")
            }
        }
        journalRecords.add(accessRecord)
        return accessRecord
    }

    fun registerAccess(instance: Any): AccessRecord<E>{
        val instanceInfo = ClassResolver.instanceInfo(instance)
        val accessRecord = AccessRecord<E>(this, ClassResolver.instanceInfo(instance).instanceName)

        val orphan = journalRecords.filter { it.recordType == defaultRecordType}

        val failRec = recordDefaults?.failureRecordType
        if(failRec != null){
            orphan.forEach {
                it.resultFailure(failRec,  instanceInfo.instanceName + "before this record was finalized.")
            }
        }
        journalRecords.add(accessRecord)
        return accessRecord
    }

    fun addRecord(message: String = ""): AccessRecord<E>{
       return registerRecord(message, defaultRecordType)
    }

    fun addRecord(recordType: E, message: String = ""): AccessRecord<E>{
        return registerRecord(message, recordType)
    }

    fun getLastRecord():AccessRecord<E>?{
       return journalRecords.lastOrNull()
    }
    fun getLastRecord(throwing: Throwing):AccessRecord<E>{
        return journalRecords.last()
    }

    fun register(recordType: E,  message: String): AccessRecord<E> = registerRecord(message, recordType)
    fun register(recordType: E,  instance: TraceableContext): AccessRecord<E>{
        val instanceInfo = ClassResolver.instanceInfo(instance)
        return registerRecord(instanceInfo.instanceName, recordType)
    }

    fun register(message: String): AccessRecord<E> = registerRecord(message, null)
    fun register(instance: TraceableContext): AccessRecord<E>{
        val instanceInfo = ClassResolver.instanceInfo(instance)
        return registerRecord(instanceInfo.instanceName, null)
    }

    fun print(): Unit = println(formattedString)
    fun clean(): Unit = journalRecords.clear()
}