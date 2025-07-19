package po.lognotify.extensions

import kotlinx.coroutines.delay
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.action.ActionSpan
import po.lognotify.common.containers.ActionContainer
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.common.containers.TaskContainer
import po.lognotify.exceptions.handleException
import po.misc.exceptions.ManagedException
import po.misc.functions.RepeatResult
import po.misc.functions.repeatIfFaulty
import po.misc.functions.repeatIfFaultySuspending
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.properties.takePropertySnapshot
import po.misc.types.castOrManaged










