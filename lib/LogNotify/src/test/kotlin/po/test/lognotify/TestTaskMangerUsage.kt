package po.test.lognotify

import org.junit.jupiter.api.Test
import po.lognotify.extensions.startTaskAsync
import po.lognotify.TasksManaged
import po.lognotify.exceptions.LoggerException
import kotlin.test.assertEquals

class LoggedClass(val className: String) : TasksManaged {

}

class TestTaskMangerUsage :  TasksManaged{
    val loggedClass = LoggedClass("LoggedClass")

    fun start(param: Int): Int = startTaskAsync("start_task"){
        param
    }.resultOrException<LoggerException>()


    @Test
    fun `startTaskAsync accepts receiver returns properly`(){
       val result =  start(10)
        assertEquals(10, result, "Return result match given")
    }
}