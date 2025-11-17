package po.test.misc.data.logging.procedural

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.context.component.asSubject
import po.misc.data.logging.LogProvider
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.ProceduralResult
import po.misc.data.logging.procedural.page
import po.misc.data.logging.processor.logProcessor
import po.misc.data.badges.Badge
import po.test.misc.data.logging.LoggerTestBase
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


class TestProceduralFlow : LoggerTestBase(), LogProvider<LogMessage>{

   private val logger = logProcessor()

   private val badge = Badge.make("temp")
   private val nestedBadge = Badge.make("nested")

   private val step1Name = "step_1"
   private val step2Name = "step_2"
   private val nestedStep1Name = "nested_1"
   private val nestedStep2Name = "nested_2"

   private val outerComponent = ProceduralTestComponent()

    @Test
    fun `Overall result calculation`(){

        val record = ProceduralFlow.createRecord(infoMsg("Subject", "Some text"))
        val flow = ProceduralFlow(logger, record)
        val badge = Badge.make("temp")
        flow.step(step1Name, badge){ true }
        flow.step(step2Name, badge){ false }
        flow.complete(true)
        assertEquals(ProceduralResult.Fail, record.result)
    }

    @Test
    fun `Procedural step registration work as expected`(){
        val record = ProceduralFlow.createRecord(infoMsg("Subject", "Some text"))
        val flow = ProceduralFlow(logger, record)
        var procEntry: ProceduralRecord? = null
        flow.processStep(step1Name, badge, tolerance = emptyList()){
            procEntry = it.proceduralRecord
        }
        assertNotNull(procEntry)
        assertNotNull(record.entries.firstOrNull()){procData->
            assertIs<ProceduralEntry>(procData)
            assertNotNull(procData.stepResult)
        }
    }

    @Test
    fun `Procedural page registration work as expected`(){
        val newProcess = "New process"
        val newProcessText = "New process text"
        val newSubject = ProceduralFlow.createRecord(infoMsg("Subject", "Some text"))
        val flow = ProceduralFlow(logger, newSubject)
        var procRec: ProceduralRecord? = null
        var subProcRec : ProceduralRecord? = null
        var receiver: Component? = null
        flow.processStep(step1Name, badge, tolerance = emptyList()){
            procRec = it.proceduralRecord
            it.page(outerComponent.processor,  newProcess, newProcessText){
                receiver = host
                subProcRec = proceduralRecord
                step(nestedStep1Name){
                }
                step(nestedStep1Name){
                }
                step(nestedStep2Name){
                    val warning = outerComponent.notification("Warning", nestedStep2Name + "warning", NotificationTopic.Warning)
                    outerComponent.processor.logData(warning.toLogMessage(), noOutput = true)
                }
           }
        }
        flow.complete(true)
        assertSame(outerComponent, receiver)
        assertNotNull(procRec){record->
            assertEquals(1, record.entries.size)
            assertEquals(newSubject.subject, record.subject)
            assertNotNull(record.entries.lastOrNull())
        }
        assertNotNull(subProcRec){subProcedural->
            assertEquals(3, subProcedural.entries.size)
            assertEquals(nestedStep2Name,  subProcedural.entries.lastOrNull()?.stepName)
            assertEquals(newProcessText, subProcedural.text)
            assertNotNull(subProcedural.entries.lastOrNull())
        }
    }

    @Test
    fun `Page block result correctly interpreted by procedural record`(){

        val info = infoMsg("New page", "Some process")
        val newSubject = ProceduralFlow.createRecord(info)
        val flow = ProceduralFlow(logger, newSubject)
        var result = listOf<Any>()
        val pageSubject =  outerComponent::startListResulting.asSubject
        flow.processStep(step1Name, badge){
            result = it.page(outerComponent.processor, pageSubject){
                host.startListResulting(10)
            }
        }
        val message = flow.complete(true)
        val record =  assertNotNull(message.logRecords.firstOrNull())

        val entryFromMessage  =  assertNotNull(message.logRecords.firstOrNull())
        assertTrue {
            record.subject.contains(pageSubject.subjectName) &&
                    entryFromMessage.subject == record.subject
        }
        assertEquals(10, result.size)
//        assertTrue {
//            flow.proceduralRecord.resultPostfix.contains("10") &&
//                    flow.proceduralRecord.resultPostfix.contains("String")
//        }
    }

    @Test
    fun `Warnings properly registered`(){
        val warning = warning("Warning", "Some warning")
        val newSubject = ProceduralFlow.createRecord(warning)
        val flow = ProceduralFlow(logger, newSubject)

        logger.useHandler(flow, LogMessage::class)
        var procRec: ProceduralRecord? = null

        flow.processStep(step1Name, badge){
            procRec = it.proceduralRecord
            logger.logData(warning.toLogMessage())
        }
        assertNotNull( procRec ){record->
            val warningMessage = assertNotNull(record.records.firstOrNull())
            val registeredWarning = assertNotNull( record.records.firstOrNull { it.topic ==  NotificationTopic.Warning } )
        }
        flow.complete()
    }

    @Test
    fun `Explicitly passed flow work same way as DSL`(){

        val startMessage = infoMsg(::`Explicitly passed flow work same way as DSL`.asSubject)
        val logProcessor = logProcessor()
        val flow = logger.createProceduralFlow(startMessage)
        logProcessor.useProcedural(flow)
        val infoMsg = infoMsg("Message", "Some text")
        logProcessor.logData(infoMsg)
        val message =  logger.finalizeFlow(flow)
        assertEquals(1, message.logRecords.size)
        assertNotNull( message.logRecords.firstOrNull { it.text ==  infoMsg.text })

        assertEquals(1, logger.logRecords.size)
        assertEquals(0, logProcessor.logRecords.size)

    }

}