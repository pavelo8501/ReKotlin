package po.lognotify.test.setup

import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler


class ForeignClass(val className: String) : TasksManaged {


   suspend fun transition(helper : TaskHandler){

//        withTask(helper) {
//            info(className)
//        }

    }

}