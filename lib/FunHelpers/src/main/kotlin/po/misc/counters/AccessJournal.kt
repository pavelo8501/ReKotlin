package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.AccessRecord
import po.misc.data.PrettyPrint
import po.misc.data.styles.SpecialChars
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo


class AccessJournalDefaults<E: Enum<E>>(
    val defaultRecordType: E,
    val successRecordType: E,
    val failureRecordType: E,
)


class AccessJournal<E: Enum<E>>(
    val hostInstanceInfo: InstanceInfo,
    val defaultRecordType: E
) : PrettyPrint {

    constructor(
        hostInstance: TraceableContext,
        recordDefaults: AccessJournalDefaults<E>
    ):this(
        ClassResolver.instanceInfo(hostInstance),
        recordDefaults.defaultRecordType
    ){
        recDefaults = recordDefaults
    }

    private var recDefaults: AccessJournalDefaults<E>? = null



    internal val journalRecords = mutableListOf<AccessRecord<E>>()
    val size : Int get() = journalRecords.size
    override val formattedString: String get() = journalRecords.joinToString(separator = SpecialChars.NEW_LINE) { it.formattedString }

    fun registerAccess(instance: Any): AccessRecord<E>{
        val instanceInfo = ClassResolver.instanceInfo(instance)
        val accessRecord = AccessRecord<E>(this, ClassResolver.instanceInfo(instance).instanceName)

        val orphan = journalRecords.filter { it.recordType == defaultRecordType}

        val failRec = recDefaults?.failureRecordType
        if(failRec != null){
            orphan.forEach {
                it.resultFailure(failRec,  instanceInfo.instanceName + "before this record was finalized.")
            }
        }
        journalRecords.add(accessRecord)
        return accessRecord
    }

    private fun registerRecord(message: String, recordType: E?): AccessRecord<E>{
        val accessRecord = AccessRecord<E>(this, message)
        recordType?.let {
            accessRecord.changeRecordType(it)
        }
        val orphan = journalRecords.filter { it.recordType == defaultRecordType}
        val failRec = recDefaults?.failureRecordType
        if(failRec != null){
            orphan.forEach {
                it.resultFailure(failRec,  message + "before this record was finalized.")
            }
        }
        journalRecords.add(accessRecord)
        return accessRecord
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




    fun print(){
        println(formattedString)
    }

    fun clean(): Unit = journalRecords.clear()

}