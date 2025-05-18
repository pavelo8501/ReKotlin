package po.test.lognotify.task

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.extensions.runTaskBlocking
import po.lognotify.extensions.subTaskAsync
import po.lognotify.logNotify

class TestTaskBase:TasksManaged {

    class ReceiverClass{

        fun function1(inputResult: Int, callback: ((TaskHandler<Int>)-> Unit)? = null): Int
            = runTaskBlocking("task_function1"){handler->

            callback?.invoke(handler)
            function2(inputResult)
        }.resultOrException()

        suspend fun function2(inputResult: Int): Int = subTaskAsync("task_function2"){

            inputResult
        }.resultOrException()
    }

    @Test
    fun `Task hierarchy creation in asynchronous mode`() = runTest{

        val receiverClass = ReceiverClass()
        logNotify().notifier.subscribeTo()
        receiverClass.function1(10)

    }

}