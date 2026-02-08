package po.test.misc.data.logging.procedural

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.component.initSubject
import po.misc.context.log_provider.LogProvider
import po.misc.data.logging.Topic
import po.misc.data.logging.log_subject.startProcSubject
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.page
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import po.test.misc.data.logging.LoggerTestBase
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProceduralLog: LoggerTestBase(), LogProvider {

    override val componentID: ComponentID = componentID("Test procedural's  log")
     override val logProcessor: LogProcessor<TestProceduralLog, LogMessage> = createLogProcessor()

    private  val subComponent1 = ProceduralTestComponent()
    private  val subComponent2 = ProceduralTestComponent()

    private val step1: String  = "Step_1"
    private val step1subStep1: String  = "Step_1_1"
    private val step1subStep2: String  = "Step_1_2"

    private val step2: String  = "Step_2"
    private val step2subStep1: String  = "Step_2_1"
    private val step2subStep2: String  = "Step_2_2"

    @BeforeEach
    fun clearLogs(){
        logProcessor.clear()
        subComponent1.processor.dropCollector()
        subComponent2.processor.dropCollector()
    }

    @Test
    fun `LogMessage successfully translated to ProceduralRecord and back to message`(){
        val logProcessor :  LogProcessor<TestProceduralLog, LogMessage> = createLogProcessor()
        val message = infoMsg(initSubject)
        var procRecord: ProceduralRecord? = null

        assertDoesNotThrow {
            logProcessor.proceduralScope(message){ procRecord = it }
        }
        assertNotNull(procRecord)
        assertNotNull(logProcessor.logRecords.firstOrNull()){
            assertEquals(message.subject, it.subject)
            assertEquals(message.text, it.text)
            assertEquals(message.topic, it.topic)
        }
    }

    @Test
    fun `LogMessages and notifications successfully translated to ProceduralEntries`(){

        val logProcessor :  LogProcessor<TestProceduralLog, LogMessage> = createLogProcessor()
        logProcessor.debugMode = true
        val message = infoMsg(initSubject)
        var procRecord: ProceduralRecord? = null
        logProcessor.proceduralScope(message){
            procRecord = it
            assertNull(logProcessor.activeUnresolved)
            logProcessor.log(notification("Subject", "Text"))
        }

        assertNotNull(procRecord?.proceduralEntries?.firstOrNull())
        assertNotNull(logProcessor.logRecords.firstOrNull()){
           val entry = assertNotNull(it.logRecords.firstOrNull())
           assertTrue { entry.subject.contains("Subject") }
        }
    }

    @Test
    fun `Foreign component's log emissions are properly registered`(){
        val logProcessor = createLogProcessor()
        val warnText = "Sub component2  Warning"
        val initMsg = infoMsg(initSubject)
        val startProcessSubject = subComponent2.startProcSubject("resulting")

        logProcessor.proceduralScope(initMsg){
            step(step1){ true }
            step(step2){
                val subject = subComponent1.startProcSubject(subComponent1::listResulting.name)
                it.page(subComponent1.processor, subject){
                    subComponent1.listResulting(5, warnOnCount = 3)
                }
            }

            page(subComponent2.processor, startProcessSubject){
                step(step1subStep1){
                    subComponent2.resulting<String>("Some result"){
                        warnText
                    }
                    "step1subStep1"
                }
            }
        }

        assertEquals(1, logProcessor.logRecords.size)
        assertEquals(0, subComponent1.processor.logRecords.size)
        assertEquals(0, subComponent2.processor.logRecords.size)

        val initialMessage =  assertNotNull(logProcessor.logRecords.firstOrNull())
        assertEquals(initMsg.text, initialMessage.text)
        assertEquals(2, initialMessage.logRecords.size)

        val initSubjectMessage =  assertNotNull(initialMessage.logRecords.firstOrNull())
        assertIs<LogMessage>(initSubjectMessage)

        val startProcessMessage =  assertNotNull(initialMessage.logRecords[1])
        assertIs<LogMessage>(startProcessMessage)
        assertEquals(startProcessSubject.subjectText, startProcessMessage.text)

        val subComponent1Warning = assertNotNull( initSubjectMessage.logRecords.firstOrNull { it.topic == Topic.Warning })
        assertEquals(subComponent1,  subComponent1Warning.context)

        val subComponent2Warning =  assertNotNull(startProcessMessage.logRecords.firstOrNull { it.text.contains(warnText) } )
        assertSame(subComponent2, subComponent2Warning.context)
    }

}