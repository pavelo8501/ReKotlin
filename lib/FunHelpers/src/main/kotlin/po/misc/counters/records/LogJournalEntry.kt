package po.misc.counters.records

import po.misc.counters.LogJournal
import po.misc.counters.records.LogJournalEntry.RecordType
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.grid.PrettyPromiseGrid
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.styles.Colour
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
    override val formattedString: String get() =  logJournalReport.render(this)
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

        private val timeStyle =  KeyedCellOptions(
          showKey = false,
          styleOptions = CellOptions.TextStyleOptions(colour = Colour.Blue)
        )
        val commentsTemplate: PrettyPromiseGrid<LogJournalEntry, Comment> = buildPrettyGrid(LogJournalEntry::comments) { list ->
            buildRow(list){
                addCell(Comment::text)
                addCell{

                }
            }
        }

        val logJournalReport: PrettyGrid<LogJournalEntry> = buildPrettyGrid<LogJournalEntry>{
            buildRow(RowPresets.Horizontal) {

                addCell(LogJournalEntry::created){

                }
                addCell(LogJournalEntry::formatedTime, timeStyle)
                addCell(LogJournalEntry::entryType)
                addCell(LogJournalEntry::message)
                addCell(LogJournalEntry::hostName)
            }
            useTemplate(commentsTemplate)
        }
    }
}