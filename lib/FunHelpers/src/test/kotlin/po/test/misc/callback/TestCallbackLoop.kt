package po.test.misc.callback

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.callbacks.loop.CallbackLoop
import po.misc.callbacks.loop.LoopConfig
import po.misc.callbacks.loop.LoopStats
import po.misc.callbacks.loop.models.DefaultOutput
import po.misc.callbacks.loop.models.OutputGroup
import po.misc.callbacks.loop.models.OutputItem
import po.misc.data.helpers.output
import po.misc.data.logging.Verbosity
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TestCallbackLoop {

    data class Update(
        var groupId: Int,
        val name: String = "Update"
    )

    class Response(
        val name: String = "Response",
        val updates: List<Update>
    )

    var callbackLoop: CallbackLoop<Response, DefaultOutput>? = null

    fun createUpdates(count: Long = 5): List<Update> {
        val result = mutableListOf<Update>()
        for (i in 1..count) {
            var groupId = 1
            if (i > 2) {
                groupId = 2
            }
            result.add(Update(groupId = groupId, name = "Update_$i"))
        }
        return result
    }

    suspend fun simulateInput(): Response {
        return Response(
            updates = createUpdates()
        )
    }

    fun simulateModification(response: Response): DefaultOutput {
        val updates = response.updates.orEmpty()
        val groups = updates.groupBy { it.groupId.toLong() }
        val outputGroups = groups.map { (groupId, groupedUpdates) ->
            OutputGroup(
                id = groupId,
                output = groupedUpdates.map { update ->
                    OutputItem(update.groupId.toLong(), update.name)
                }
            )
        }
        return DefaultOutput(outputGroups)
    }

    suspend fun loopOutput(output: DefaultOutput) {
        output.output()
        callbackLoop?.stop()
    }


    @Test
    fun `Callback loop instantiation and single run`() = runTest {

        var onLoopData: Any? = null

        callbackLoop = CallbackLoop<Response, DefaultOutput>(LoopConfig(verbosity = Verbosity.Warnings)) {
            callbacks.onRequest(::simulateInput)
            callbacks.onModification(::simulateModification)
            callbacks.onResponse(::loopOutput)

            onLoop {
                onLoopData = it
            }
        }
        assertDoesNotThrow {
            callbackLoop!!.start()
        }
        assertIs<DefaultOutput>(onLoopData)
    }

    @Test
    fun `Callback loop produces exactly 3 outputs`() = runTest {

        var loopOutputCounter: Int = 0

        val loop = CallbackLoop<Response, DefaultOutput> {
            callbacks.onRequest(::simulateInput)
            callbacks.onModification(::simulateModification)
            callbacks.onResponse(::loopOutput)

            onLoop {
                loopOutputCounter ++
                if(loopOutputCounter >= 3){
                    stop()
                }
            }
        }

        loop.startAndWait()
        assertEquals(3, loopOutputCounter)
    }
}