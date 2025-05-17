package po.test.lognotify.task

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.extensions.newTask
import po.lognotify.extensions.subTask
import po.misc.exceptions.HandlerType
import kotlin.test.assertEquals

class TestTaskRunner {


    suspend fun task2() : String = subTask("sub_task"){
        val result : String = "result"
        throw Exception("task2 Exception")
        result
    }.resultOrException()

    suspend fun rootTask(testScope : TestScope): String = newTask("task", testScope.coroutineContext){handler->
        handler.handleFailure(HandlerType.GENERIC){
            "result"
        }
        task2()
    }.resultOrException()


    @Test
    fun `exception management work`() = runTest{
        val result =  rootTask(this)
        assertEquals("result", result)


    }


}