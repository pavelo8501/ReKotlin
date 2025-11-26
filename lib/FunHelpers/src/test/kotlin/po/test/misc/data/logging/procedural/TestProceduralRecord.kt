package po.test.misc.data.logging.procedural

import po.misc.collections.asList
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.log_provider.LogProvider
import po.misc.data.badges.Badge
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.log_subject.startProcSubject
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.StepResult
import po.misc.data.logging.processor.createLogProcessor
import po.misc.types.token.TokenFactory
import kotlin.test.Test

class TestProceduralRecord : LogProvider, TokenFactory {

    override val componentID: ComponentID = componentID("Process # 1")

    override val logProcessor = createLogProcessor()

    @Test
     fun `Output produced`() {

        val startProcessSubject = startProcSubject(::`Output produced`)
        val record = ProceduralRecord(startProcessSubject.toLogMessage(this))
        val warning = warning("Some subject", "Warning text")
        val entry = ProceduralEntry(Badge.Warning, "Step 1", StepResult.Warning(warning.asList()), record)
        record.proceduralEntries.add(entry)
        record.outputRecord()
     }

}