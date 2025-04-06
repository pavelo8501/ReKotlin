package po.lognotify.test.setup

import po.managedtask.classes.task.TaskHelper
import po.managedtask.extensions.withTask
import po.managedtask.interfaces.TasksManaged
import po.managedtask.models.TaskKey

class ForeignClass(val className: String) : TasksManaged {


   suspend fun transition(helper : TaskHelper){

//        withTask(helper) {
//            info(className)
//        }

    }

}