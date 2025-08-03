package po.lognotify.exceptions

import po.lognotify.common.LNInstance
import po.lognotify.notification.models.FailureReasoning
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.misc.exceptions.ManagedException

internal interface ExceptionBehaviour {
    /**
     * Later to add more advanced logic for ActionSpan
     */

    companion object {
        val onExceptionBehaviour: (ManagedException, LNInstance<*>) -> Unit = { th, lnInstance ->
            val failure = FailureReasoning(lnInstance.identifiedByName, lnInstance.nestingLevel, false)
            lnInstance.rootTask.saveFailureReasoning(failure)
            lnInstance.complete(th)
        }
    }
}
