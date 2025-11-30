package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.parts.AccessJournalDefaults
import po.misc.counters.records.AccessRecord
import po.misc.counters.records.LogJournalEntry
import po.misc.counters.records.LogJournalEntry.RecordType
import po.misc.data.splitLines
import po.misc.data.strings.stringify
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceInfo
import po.misc.types.castOrThrow
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
                LogJournalEntry.logJournalReport.render(it)
            }
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
}

infix fun  LogJournalEntry.comment(text: Any):LogJournalEntry{
    addComment(text.stringify().formatedString)
   return this
}

