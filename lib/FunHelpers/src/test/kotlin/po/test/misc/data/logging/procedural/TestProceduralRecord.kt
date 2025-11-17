package po.test.misc.data.logging.procedural

import po.misc.collections.asList
import po.misc.context.component.ComponentID
import po.misc.context.component.asSubject
import po.misc.context.component.componentID
import po.misc.data.badges.Badge
import po.misc.data.logging.LogProvider
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.procedural.StepResult
import po.misc.types.token.TokenFactory
import po.test.misc.data.logging.processor.CustomNotification
import kotlin.test.Test

class TestProceduralRecord : LogProvider<CustomNotification>, TokenFactory {

    override val componentID: ComponentID = componentID("Process # 1")

    @Test
     fun `Output produced`() {
        val startProcessSubject = this::`Output produced`.asSubject
        val record = ProceduralRecord(this, startProcessSubject)
        val warning = warning("Some subject", "Warning text")
        val entry = ProceduralEntry(Badge.Warning, "Step 1", StepResult.Warning(warning.asList()))
        record.entries.add(entry)
        record.outputRecord()
     }

}