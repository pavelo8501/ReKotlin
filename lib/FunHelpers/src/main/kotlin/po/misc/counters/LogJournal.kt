package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.parts.AccessJournalDefaults
import po.misc.counters.records.LogJournalEntry
import po.misc.counters.records.LogJournalEntry.RecordType
import po.misc.data.strings.stringify
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo
import po.misc.types.safeCast


class LogJournal(
    hostInstanceInfo: InstanceInfo,
    defaultRecordType: RecordType = RecordType.Entry,
    recordDefaults: AccessJournalDefaults<RecordType>? = null
) : JournalBase<RecordType>(hostInstanceInfo, defaultRecordType, recordDefaults){

    constructor(
        hostInstance: TraceableContext,
        defaultRecordType: RecordType = RecordType.Entry,
        recordDefaults: AccessJournalDefaults<RecordType>? = null
    ):this(ClassResolver.instanceInfo(hostInstance), defaultRecordType, recordDefaults)

    var activeType: RecordType  = defaultRecordType
    val logRecords : List<LogJournalEntry> get() = journalRecords.filterIsInstance<LogJournalEntry>()

    override val formattedString: String
        get() {
           return logRecords.joinToString {
                LogJournalEntry.entryTemplate.render(it)
            }
        }

    private fun createRecord(message: String, recordType: RecordType?):LogJournalEntry{
        return LogJournalEntry(message, recordType?:activeType, this)
    }

    private fun addEntry(entry:LogJournalEntry){
        journalRecordsBacking.add(entry)
    }

    override  fun registerRecord(message: String, recordType: RecordType?): LogJournalEntry{
        val type = recordType?.safeCast<RecordType>()?: activeType
        val accessRecord = LogJournalEntry(message, type, this)
        journalRecordsBacking.add(accessRecord)
        return accessRecord
    }

    fun addRecord(text: String, recordType: RecordType, commentWriter: LogJournalEntry.()-> Unit):LogJournalEntry{
        val record = LogJournalEntry(text, recordType, this)
        commentWriter.invoke(record)
        journalRecordsBacking.add(record)
        return record
    }

    fun addRecord(text: String, commentWriter: LogJournalEntry.()-> Unit):LogJournalEntry =
        addRecord(text,activeType, commentWriter)

    fun logMethod(methodName:String, vararg parameters: Any){
        val params = parameters.stringify().toString()
        val text = "$methodName $params"
        addEntry(createRecord(text, RecordType.Entry))
    }

    fun <T: Any, R> startWrite(receiver:T,  recordType: RecordType, block: LogJournal.(T)-> R):R{
        val journal = this@LogJournal
        activeType = recordType
        val result =  block.invoke(journal, receiver)
        activeType = defaultRecordType
        return result
    }

}

infix fun  LogJournalEntry.comment(text: Any):LogJournalEntry{
    addComment(text.stringify().toString())
   return this
}

