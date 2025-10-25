package po.test.misc.data.logging.procedural

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.logging.LogProvider
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.StepResult
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.test.misc.data.logging.processor.CustomNotification
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestProceduralRecord : LogProvider<CustomNotification>, TokenFactory {

    private class TestLogData(
        private val provider: LogProcessor<TestProceduralRecord, TestLogData>,
        override val subject: String
    ): PrintableBase<TestLogData>(this), StructuredLoggable, Loggable {

        override val created: Instant = Instant.now()

        override val self: TestLogData = this

        override val text: String = ""
        override val context: TraceableContext = provider.host
        override val topic: NotificationTopic =  NotificationTopic.Info

        val proceduralRecords = mutableListOf<ProceduralEntry>()

        override fun registerRecord(record: ProceduralEntry) {
            proceduralRecords.add(record)
        }

        companion object: PrintableCompanion<TestLogData>(TypeToken.create<TestLogData>())
    }

    override val componentID: ComponentID = componentID("Process # 1")

    private val provider = LogProcessor<TestProceduralRecord, TestLogData>(this, TypeToken.create())

    @Test
    fun `Logging process functionality`() {

        val info = TestLogData(provider, "Initializing")

        provider.logScope(info){
            val step1Result = proceduralStep("Step_1") {
                val returning: Int = 300
                returning
            }
            assertEquals(300, step1Result)

            val step2Result = assertDoesNotThrow {
                proceduralStep("Step_2") {
                    val resStr: String? = null
                    resStr
                }
            }
            assertEquals(null, step2Result)
        }

        val logRecord = assertNotNull(provider.records.firstOrNull())
        assertEquals(2, logRecord.proceduralRecords.size)
        val firstRec: ProceduralEntry = logRecord.proceduralRecords.first()
        val secondRec = logRecord.proceduralRecords[1]
        assertEquals("Step_1", firstRec.stepName)
        assertEquals(StepResult.Fail, secondRec.stepResult)
        firstRec.output()
        secondRec.output()
    }

}