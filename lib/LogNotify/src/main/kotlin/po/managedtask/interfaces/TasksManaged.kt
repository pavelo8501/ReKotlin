package po.managedtask.interfaces

import po.lognotify.logging.LoggingService
import po.managedtask.classes.ResultantTask
import po.managedtask.exceptions.ManagedExceptionBase


interface TasksManaged {

    val personalName: String

    companion object {
        val logger = LoggingService()
    }

}