package po.test.misc.data.logging.processor.parts

import org.junit.jupiter.api.Test
import po.misc.data.logging.procedural.ProceduralFlow
import po.test.misc.data.logging.LoggerTestBase
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestStructuredProperty: LoggerTestBase() {


    private val foreignContext = ProceduralTestComponent()

    @Test
    fun `Template contracts integration to StructuredProperty`(){

        val message = initialMessage()
        val procedural = ProceduralFlow.toProceduralRecord(message)

        procedural.structuredOptions.disableSideEffects = false

        val message1 = generateMessage(1)
        procedural.logRecords.add(message1)

        val foreignMessage = foreignContext.generateMessage(1)
        procedural.logRecords.add(foreignMessage)

        assertNotNull(procedural.proceduralEntries.firstOrNull()){entry->
            assertEquals(1, entry.proceduralRecords.size)
        }
    }

}