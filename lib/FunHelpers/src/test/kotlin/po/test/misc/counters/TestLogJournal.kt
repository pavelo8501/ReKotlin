package po.test.misc.counters

import org.junit.jupiter.api.Test
import po.misc.counters.LogJournal
import po.misc.counters.LogWriter
import po.misc.counters.comment
import po.misc.counters.records.LogJournalEntry.RecordType
import po.misc.data.output.output
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TestLogJournal : LogWriter {

    override val journal : LogJournal =  LogJournal(this,  RecordType.Entry)

    @Test
    fun `Log journal convenience methods`(){
        startWrite {
            addRecord("Some message")
        }
        startWrite(RecordType.False) {
            addRecord("Some failure")
            addRecord("Some failure2")
            addRecord("Some failure3")
            addRecord("Some failure4")
        }
        assertEquals(5, journal .size)
        assertEquals(4, journal.journalRecords.filter { it.entryType == RecordType.False }.size )
        assertNotNull(journal.journalRecords.lastOrNull()){
            assertEquals("Some failure4", it.message)
            assertEquals(RecordType.False, it.entryType)
        }
    }

    @Test
    fun `LogJournal comments creation logic`(){
        journal.clear()
        startWrite(RecordType.OK) {
            addRecord("Some message"){ comment("Some comment")
            } comment "Some infix comment :)" comment "Another one :)))))"
        }

        assertNotNull(journal.logRecords.firstOrNull()){record->
            assertEquals(3,  record.comments.size)
        }
        journal.output()
    }

    @Test
    fun `Log journal templating work`(){
        val record = journal.addRecord("Message")
        record.output()
    }


}