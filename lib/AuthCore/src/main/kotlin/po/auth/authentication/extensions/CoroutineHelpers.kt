package po.auth.authentication.extensions

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


data class CoroutineInfo(
    val name: String,
    val hashCode: Int,
    val dispatcherName: String,
    val jobStatus: String,
    val childJobCount: Int,
    val topLevelKeys : List<String>
)

suspend fun CoroutineContext.getCoroutineInfo():CoroutineInfo {

    val hashCode = coroutineContext.hashCode()
    val coroutineName = coroutineContext[CoroutineName]?.name ?: "N/A"
    val dispatcher = coroutineContext[ContinuationInterceptor]?.javaClass?.simpleName ?: "UnknownDispatcher"

    val job = coroutineContext[Job]
    val jobStatus = if (job?.isActive == true) "Active" else "Inactive or Completed"
    val childJobs = job?.children?.toList()?.size ?: 0

    val elementsDump = coroutineContext.fold(mutableListOf<String>()) { acc, element ->
        val valueType = element::class.simpleName.toString()
        acc.add(valueType)
        acc
    }

    return CoroutineInfo(coroutineName, hashCode, dispatcher, jobStatus, childJobs, elementsDump)
}