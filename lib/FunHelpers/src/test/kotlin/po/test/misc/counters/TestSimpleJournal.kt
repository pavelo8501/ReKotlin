package po.test.misc.counters

import po.misc.counters.SimpleJournal
import po.misc.data.linesCount
import po.misc.data.output.output
import po.misc.data.styles.Colour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestSimpleJournal {

    private val journal = SimpleJournal("TestSimpleJournal report")

    @Test
    fun `Data record templating test`() {
        journal.info("Info message")
        val records = journal.records
        var record = assertNotNull(records.firstOrNull())
        assertTrue { record.formattedString.contains(Colour.Blue.code) }
        assertTrue { record.formattedString.contains(Colour.GreenBright.code) }

        journal.warning("Warning message")
        record = assertNotNull(records.lastOrNull())
        assertTrue { record.formattedString.contains(Colour.YellowBright.code) }

        journal.success("Success message")
        record = assertNotNull(records.lastOrNull())
        assertTrue { record.formattedString.contains(Colour.GreenBright.code) }
        assertEquals(4, journal.formattedString.linesCount)
    }

}