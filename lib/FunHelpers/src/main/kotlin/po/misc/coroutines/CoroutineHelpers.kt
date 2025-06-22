package po.misc.coroutines


import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext


data class CoroutineInfo(
    val name: String,
    val hashCode: Int,
    val dispatcherName: String,
    val jobStatus: String,
    val childJobCount: Int,
    val topLevelKeys : List<String>
){


    companion object{

        fun createInfo(coroutineContext: CoroutineContext?):CoroutineInfo{
            return  if(coroutineContext != null){
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
                 CoroutineInfo(coroutineName, hashCode, dispatcher, jobStatus, childJobs, elementsDump)
            }else{
                 CoroutineInfo("Context Unavailable", 0, "Context Unavailable", "Context Unavailable", 0, emptyList())
            }
        }
    }
}



suspend fun CoroutineScope.getCoroutineInfo():CoroutineInfo {
    return  CoroutineInfo.createInfo(coroutineContext)
}