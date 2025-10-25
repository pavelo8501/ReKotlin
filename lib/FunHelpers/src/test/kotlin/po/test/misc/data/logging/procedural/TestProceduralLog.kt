package po.test.misc.data.logging.procedural

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.LogProvider
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.Notification
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.StepResult
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.logProcessor
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProceduralLog: ProceduralTest(), LogProvider<ProceduralRecord> {

    override val componentID: ComponentID = componentID("Log Provider")
    val processor: LogProcessor<TestProceduralLog, ProceduralRecord> = logProcessor{
        ProceduralRecord(it)
    }
    private  val subComponent1 = Component1()
    private  val subComponent2 = Component2()

    private fun processNotification(notification: Notification){
        val procedural = notification.toProceduralEntry()
        processor.activeOrNew.procedural.add(procedural)
    }


    private fun processProceduralRecord(procedural: ProceduralRecord){
        processor.logData(procedural)
    }

    @BeforeEach
    fun clearLogs(){
        processor.clear()
        subComponent1.processor.dropCollector()
        subComponent2.processor.dropCollector()
    }

    @Test
    fun `Simple usage test`(){
        val startingMessage = notification(NotificationTopic.Info, "Simple Usage", "Start")
        processor.logRecord(startingMessage)
        assertEquals(1, processor.records.size)
        subComponent1.processor.collectData(keepData = false, ::processNotification)
        subComponent2.processor.collectData(keepData = false, ::processNotification)
        processor.logScope(processor.activeOrNew){
            subComponent1.emmitInfo("Process_1")
            subComponent2.emmitInfo("Process_2")
        }
        val firstRecord = processor.records.first()
        assertEquals(2, firstRecord.procedural.size)
        assertEquals(0, subComponent1.processor.records.size)
        assertEquals(0, subComponent2.processor.records.size)
    }

    @Test
    fun `Hosting component overrides outputs inside step lambda`(){
        val startingMessage = notification(NotificationTopic.Info, "Simple Usage", "Start")
        val activeRecord = processor.activeOrNew
        processor.logScope(activeRecord){
            proceduralStep("Step_1", logRecord){ record->
                subComponent1.processor.withProcedural(record){
                    subComponent1.emmitInfoWithResult("Process_1", true)
                }
                subComponent1.processor.withProcedural(record){
                    subComponent1.emmitInfoWithResult("Process_2", false)
                }
                subComponent2.processor.withProcedural(record){
                    subComponent2.emmitInfoWithResult("Process_1", null)
                }
            }
        }
        assertEquals(1, processor.records.size)
        val proceduralList =  processor.records.first().procedural
        assertEquals(1, proceduralList.size)
        val proceduralRec =  proceduralList.first()
        assertEquals(StepResult.Fail, proceduralRec.stepResult)
        assertEquals(3, proceduralRec.subEntries.size)

    }
}