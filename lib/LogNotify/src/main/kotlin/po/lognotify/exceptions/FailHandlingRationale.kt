package po.lognotify.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException


enum class ActionResult{
    HandledByOnFailureCause,
    HandledByOnFail,
    EscalatedByResultOrException,
    ResultNotAccessed
}

enum class FailAction{
    EscalateIfNoFallback,
    Escalate,
    DoNothing
}

class FailHandlingRationale(
    val isRootTask: Boolean,
    val coroutineOwner: Boolean,
    val nestingLevel: Int,
    managed: ManagedException,
){

    var resultAction: ActionResult = ActionResult.ResultNotAccessed
        private set

    val exceptionHandler: HandlerType = managed.handler

    val failAction: FailAction get() {
        var action:FailAction =  FailAction.DoNothing
        if(isRootTask && exceptionHandler == HandlerType.SkipSelf){
            action = FailAction.EscalateIfNoFallback
        }
        if(isRootTask && exceptionHandler == HandlerType.CancelAll){
            action = FailAction.Escalate
        }
        if(nestingLevel > 0){
            action =  FailAction.DoNothing
        }
       return action
    }

    fun setAction(action: ActionResult){
        resultAction = action
    }
}