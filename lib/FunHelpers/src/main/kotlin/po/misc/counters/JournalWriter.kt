package po.misc.counters

import po.misc.context.tracable.TraceableContext
import po.misc.counters.records.LogJournalEntry
import po.misc.counters.records.LogJournalEntry.RecordType


interface JournalWriter<E: Enum<E>> : TraceableContext {

    val journal : JournalBase<E>

    fun <T: Any, R> T.startWrite(block : JournalBase<*>.(T)-> R):R{
       return block.invoke(journal, this)
    }

    fun <T: Any,  R> T.startWrite(recordType: E,   block : JournalBase<*>.(T)-> R):R{
        journal.useForEntries(recordType)
        return block.invoke(journal, this)
    }
}
interface LogWriter: TraceableContext {

    val journal : LogJournal

    fun <T: Any, R> T.startWrite(block : JournalBase<*>.(T)-> R):R{
        return block.invoke(journal, this)
    }
    fun <T: Any, R> T.startWrite(recordType: RecordType, block: JournalBase<*>.(T)-> R):R{
        journal.useForEntries(recordType)
        val result = block.invoke(journal, this)
        journal.resetEntryType()
        return result
    }
    fun addRecord(text: String, commentWriter: LogJournalEntry.()-> Unit):LogJournalEntry = journal.addRecord(text, commentWriter)
}