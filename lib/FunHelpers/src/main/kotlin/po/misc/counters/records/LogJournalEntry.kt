package po.misc.counters.records

import po.misc.counters.LogJournal
import po.misc.counters.records.LogJournalEntry.RecordType
import po.misc.time.TimeHelper
import java.time.Instant


class LogJournalEntry(
    override val message: String,
    override val entryType: RecordType,
    private val logJournal: LogJournal,
): JournalEntry<RecordType>, TimeHelper {

    enum class RecordType { Entry, OK, False }
    data class Comment(val text: String)
    val created: Instant = Instant.now()
    val hostName: String = logJournal.hostInstanceInfo.instanceName
    val formatedTime: String = created.hoursFormated(2)

    override val formattedString: String get() = message
    val comments: MutableList<Comment> = mutableListOf()

    override fun resultOK(successType: RecordType, message: String?) {

    }
    override fun resultFailure(failureType: RecordType, reason: String){

    }
    fun addComment(text: String):LogJournalEntry{
        comments.add(Comment(text))
        return this
    }

    companion object{

//        private val timeStyle = Options(renderKey = false, style = Style(colour = Colour.Blue))
//        val entryTemplate : PrettyRow<LogJournalEntry> = buildPrettyRow {
//            addAll(LogJournalEntry::formatedTime, LogJournalEntry::entryType,LogJournalEntry::message, LogJournalEntry::hostName)
//        }
    }
}