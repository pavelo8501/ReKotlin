package po.test.misc.data.logging.processor.contracts

import org.junit.jupiter.api.Test
import po.misc.data.logging.Topic
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralResult
import po.misc.data.logging.procedural.StepResult
import po.misc.data.logging.processor.contracts.ProceduralContract
import po.test.misc.data.logging.LoggerTestBase
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestNewTemplateNode: LoggerTestBase() {


    private val foreignContext = ProceduralTestComponent()

    @Test
    fun `ProceduralContract's success entries`(){

        val initMsg = infoMsg("Initial", "Initial start message")
        val procedural = ProceduralFlow.toProceduralRecord(initMsg)
        val newNode = ProceduralContract(procedural)

        val secondMessage = infoMsg("Second", "second message")
        newNode.addRecord(secondMessage)
        val firstEntry = assertNotNull(procedural.proceduralEntries.firstOrNull())
        assertEquals(0, firstEntry.records.size)

        val foreignMessage = foreignContext.generateMessage(1)
        newNode.addRecord(foreignMessage)
        assertEquals(1, firstEntry.records.size)

        val messages = foreignContext.generateMessages("sub message", 2)
        messages.forEach {
            newNode.addRecord(it)
        }
        val firstSubProcedural = assertNotNull(firstEntry.records.firstOrNull())
        firstSubProcedural.proceduralEntries.size
        assertEquals(2, firstSubProcedural.proceduralEntries.size)

        assertIs<StepResult.OK>( firstSubProcedural.proceduralEntries.first().stepResult)
        assertIs<StepResult.OK>( firstSubProcedural.proceduralEntries[1].stepResult)
        assertEquals( ProceduralResult.Ok, procedural.result)
        procedural.outputRecord()
    }

    @Test
    fun `ProceduralContract's warnings entries`(){

        val initMsg = infoMsg("Initial", "Initial message for warnings")
        val procedural = ProceduralFlow.toProceduralRecord(initMsg)
        val newNode = ProceduralContract(procedural)
        val secondMessage = infoMsg("Second", "second main message")
        val thirdMessage = infoMsg("Third", "third main message")
        newNode.addRecord(secondMessage)
        newNode.addRecord(thirdMessage)

        val foreignMessage = foreignContext.generateMessage("Foreign", "start")
        newNode.addRecord(foreignMessage)
        val warnings = foreignContext.generateMessages("sub message", 2, Topic.Warning)
        newNode.addRecords(warnings)

        val lastEntry = assertNotNull(procedural.proceduralEntries.lastOrNull())
        val firstSubProcedural = assertNotNull(lastEntry.records.firstOrNull())
        assertEquals(2, firstSubProcedural.proceduralEntries.size)

        assertIs<StepResult.Warning>( firstSubProcedural.proceduralEntries.first().stepResult)
        assertIs<StepResult.Warning>( firstSubProcedural.proceduralEntries[1].stepResult)
        assertEquals( ProceduralResult.Warning, procedural.result)

        procedural.outputRecord()
    }

}