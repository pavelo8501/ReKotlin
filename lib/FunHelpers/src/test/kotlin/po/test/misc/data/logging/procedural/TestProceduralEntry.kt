package po.test.misc.data.logging.procedural

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.StepResult
import po.misc.data.logging.procedural.StepTolerance
import po.misc.data.badges.Badge
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TestProceduralEntry: TraceableContext {

    val entry = ProceduralEntry(Badge.Init,"step")

    @Test
    fun `Step result correctly analyzed`(){
        val stringList = listOf("String1", "string2")
        var listResult = ProceduralFlow.toStepResult(entry,  stringList)
        assertIs<StepResult.OK>(listResult)

        listResult = ProceduralFlow.toStepResult(entry, result =  emptyList<String>(), tolerances = emptyList())
        assertIs<StepResult.Fail>(listResult)

        var booleanResult = ProceduralFlow.toStepResult(entry, true)
        assertIs<StepResult.OK>(booleanResult)

        booleanResult = ProceduralFlow.toStepResult(entry,false)
        assertIs<StepResult.Fail>(booleanResult)

        val nullResult = ProceduralFlow.toStepResult(entry,null)
        assertIs<StepResult.Fail>(nullResult)

        val unitResult = ProceduralFlow.toStepResult(entry,Unit)
        assertIs<StepResult.OK>(unitResult)
    }

    @Test
    fun `Step result tolerance work as expected`(){
        val listResult = ProceduralFlow.toStepResult(entry,emptyList<String>(), StepTolerance.ALLOW_EMPTY_LIST)
        assertIs<StepResult.OK>(listResult)

        val booleanResult = ProceduralFlow.toStepResult(entry,false, StepTolerance.ALLOW_FALSE)
        assertIs<StepResult.OK>(booleanResult)

        val nullResult = ProceduralFlow.toStepResult(entry,null, StepTolerance.ALLOW_NULL)
        assertIs<StepResult.OK>(nullResult)
    }

}