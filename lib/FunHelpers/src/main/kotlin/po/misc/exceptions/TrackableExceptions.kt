package po.misc.exceptions

import po.misc.coroutines.CoroutineInfo
import po.misc.coroutines.coroutineInfo
import po.misc.exceptions.models.ExceptionTrace
import kotlin.reflect.KClass


interface TrackableException{
    val exceptionTrace: ExceptionTrace
    val self : Throwable
    val contextClass: KClass<*>
}



interface TrackableScopedException: TrackableException {
   var coroutineInfo: CoroutineInfo?

}