package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.parts.AccessJournalDefaults
import po.misc.counters.records.AccessRecord
import po.misc.counters.records.JournalEntry
import po.misc.data.PrettyPrint
import po.misc.data.styles.SpecialChars
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo
import po.misc.functions.Throwing


abstract class JournalBase<E: Enum<E>>(
    val hostInstanceInfo: InstanceInfo,
    val defaultRecordType:E,
    var recordDefaults: AccessJournalDefaults<E>? = null

) : PrettyPrint {

    internal val journalRecordsBacking: MutableList<JournalEntry<E>> = mutableListOf<JournalEntry<E>>()
    val journalRecords: List<JournalEntry<E>> get() = journalRecordsBacking

     open var activeRecordType :  E = defaultRecordType

    val size : Int get() = journalRecords.size
    override val formattedString: String get() = journalRecords.joinToString(separator = SpecialChars.NEW_LINE) { it.formattedString }

    protected open fun registerRecord(message: String, recordType: E? ): JournalEntry<E> {
        val type = recordType?: activeRecordType
        val accessRecord = AccessRecord<E>(message, this)
        accessRecord.entryType  = type
        val orphan = journalRecords.filter { it.entryType == defaultRecordType}
        val failRec = recordDefaults?.failureRecordType
        if(failRec != null){
            orphan.forEach {
                it.resultFailure(failRec,  message + "before this record was finalized.")
            }
        }
        journalRecordsBacking.add(accessRecord)
        return accessRecord
    }

    fun useForEntries(recordType: E){
        activeRecordType = recordType
    }

    fun resetEntryType(){
        activeRecordType = defaultRecordType
    }

    fun registerAccess(instance: Any): AccessRecord<E> {
        val instanceInfo = ClassResolver.instanceInfo(instance)
        val accessRecord = AccessRecord<E>(ClassResolver.instanceInfo(instance).instanceName, this)
        val orphan = journalRecords.filter { it.entryType == defaultRecordType}
        val failRec = recordDefaults?.failureRecordType
        if(failRec != null){
            orphan.forEach {
                it.resultFailure(failRec,  instanceInfo.instanceName + "before this record was finalized.")
            }
        }
        journalRecordsBacking.add(accessRecord)
        return accessRecord
    }

    fun addRecord(message: String = ""):  JournalEntry<E> {
       return registerRecord(message, activeRecordType)
    }

    fun addRecord(message: String, recordType: E? = null):  JournalEntry<E>{
        return registerRecord(message, recordType)
    }
    fun register(recordType: E,  instance: TraceableContext): JournalEntry<E> {
        val instanceInfo = ClassResolver.instanceInfo(instance)
        return registerRecord(instanceInfo.instanceName, recordType)
    }
    fun register(recordType: E,  message: String):JournalEntry<E> = registerRecord(message, recordType)


    fun register(instance: TraceableContext): JournalEntry<E> {
        val instanceInfo = ClassResolver.instanceInfo(instance)
        return registerRecord(instanceInfo.instanceName, null)
    }
    fun register(message: String):JournalEntry<E> = registerRecord(message, null)

    fun print(): Unit = println(formattedString)
    fun clear(): Unit = journalRecordsBacking.clear()
}