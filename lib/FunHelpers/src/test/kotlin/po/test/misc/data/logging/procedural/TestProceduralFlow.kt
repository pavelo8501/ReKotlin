package po.test.misc.data.logging.procedural

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.context.log_provider.LogProvider
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.ProceduralResult
import po.misc.data.logging.procedural.page
import po.misc.data.logging.processor.createLogProcessor
import po.misc.data.badges.Badge
import po.misc.data.logging.log_subject.startProcSubject
import po.test.misc.data.logging.LoggerTestBase
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue


class TestProceduralFlow : LoggerTestBase(), LogProvider{

   override val logProcessor = createLogProcessor()

   private val badge = Badge.make("temp")
   private val nestedBadge = Badge.make("nested")

   private val step1Name = "step_1"
   private val step2Name = "step_2"
   private val nestedStep1Name = "nested_1"
   private val nestedStep2Name = "nested_2"

   private val outerComponent = ProceduralTestComponent()

    @Test
    fun `Overall result calculation`(){
        val message = infoMsg("Subject", "Some text")
        val record = ProceduralFlow.toProceduralRecord(message)
        val flow = ProceduralFlow(logProcessor, record)
        val badge = Badge.make("temp")
        flow.step(step1Name, badge){ true }
        flow.step(step2Name, badge){ false }
        flow.complete(true)
        assertEquals(ProceduralResult.Fail, record.result)
    }

    @Test
    fun `Procedural step registration work as expected`(){
        val message = infoMsg("Subject", "Some text")
        val record = ProceduralFlow.toProceduralRecord(message)
        val flow = ProceduralFlow(logProcessor, record)
        var procEntry: ProceduralRecord? = null
        flow.processStep(step1Name, badge, tolerance = emptyList()){
            procEntry = it.proceduralRecord
        }
        assertNotNull(procEntry)
        assertNotNull(record.proceduralEntries.firstOrNull()){procData->
            assertIs<ProceduralEntry>(procData)
            assertNotNull(procData.stepResult)
        }
    }

    @Test
    fun `Procedural page registration work as expected`(){
        val newProcess = "New process"
        val newProcessText = "New process text"
        val newSubject = ProceduralFlow.toProceduralRecord(infoMsg("Subject", "Some text"))
        val flow = ProceduralFlow(logProcessor, newSubject)
        var procRec: ProceduralRecord? = null
        var subProcRec : ProceduralRecord? = null
        var receiver: Component? = null
        flow.processStep(step1Name, badge, tolerance = emptyList()){
            procRec = it.proceduralRecord
            it.page(outerComponent.processor,  newProcess, newProcessText){
                receiver = host
                subProcRec = proceduralRecord
                step(nestedStep1Name){}
                step(nestedStep1Name){}
                step(nestedStep2Name){
                    val warning = outerComponent.notification("Warning", nestedStep2Name + "warning", NotificationTopic.Warning)
                    outerComponent.processor.logData(warning.toLogMessage(), noOutput = true)
                }
           }
        }
        flow.complete(true)
        assertSame(outerComponent, receiver)
        assertNotNull(procRec){record->
            assertEquals(1, record.proceduralEntries.size)
            assertEquals(newSubject.subject, record.subject)
            assertNotNull(record.proceduralEntries.lastOrNull())
        }
        assertNotNull(subProcRec){subProcedural->
            assertEquals(3, subProcedural.proceduralEntries.size)
            assertEquals(nestedStep2Name,  subProcedural.proceduralEntries.lastOrNull()?.stepName)
            assertEquals(newProcessText, subProcedural.text)
            assertNotNull(subProcedural.proceduralEntries.lastOrNull())
        }
    }

    @Test
    fun `Page block result correctly interpreted by procedural record`(){

        val info = infoMsg("New page", "Some process")
        val newSubject = ProceduralFlow.toProceduralRecord(info)
        val flow = ProceduralFlow(logProcessor, newSubject)
        var result = listOf<Any>()
        val pageSubject =  startProcSubject(outerComponent::startListResulting)
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
    }

    @Test
    fun `Warnings properly registered`(){
        val warning = warning("Warning", "Some warning")
        val newSubject = ProceduralFlow.toProceduralRecord(warning)
        val flow = ProceduralFlow(logProcessor, newSubject)

        logProcessor.useHandler(flow, LogMessage::class)
        var procRec: ProceduralRecord? = null

        flow.processStep(step1Name, badge){
            procRec = it.proceduralRecord
            logProcessor.logData(warning.toLogMessage())
        }
        assertNotNull( procRec ){record->
            val warningMessage = assertNotNull(record.logRecords.firstOrNull())
            val registeredWarning = assertNotNull( record.logRecords.firstOrNull { it.topic ==  NotificationTopic.Warning } )
        }
        flow.complete()
    }

}