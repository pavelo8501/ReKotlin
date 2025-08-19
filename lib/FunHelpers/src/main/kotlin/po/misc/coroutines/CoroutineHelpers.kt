package po.misc.coroutines


import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import po.misc.data.styles.SpecialChars
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext


data class CoroutineInfo(
    val coroutineName: String = "Context Unavailable",
    val hashCode: Int = -1,
    val dispatcherClass: String = "Context Unavailable",
    val dispatcherName: String = "Context Unavailable",
    val jobStatus: String = "N/A",
    val childJobCount: Int = -1,
    val topLevelKeys : List<String> = emptyList()
){



    override fun toString(): String {
        val keys = topLevelKeys.joinToString(prefix = "Top level keys:[", separator = "/", postfix = "]") { it }
        return buildString {
            appendLine("Coroutine Name: $coroutineName")
            appendLine("Dispatcher Name: $dispatcherName")
            appendLine("Job Status: $jobStatus")
            appendLine("Job Status: $jobStatus")
            appendLine("Child Jobs: $childJobCount")
            appendLine(keys)
        }
    }

    fun echo(){
        println(this)
    }

    companion object{

        private fun dispatcherPrettyName(dispatcher: ContinuationInterceptor?): String {
            if (dispatcher == null) return "Unknown"
            return when (dispatcher) {
                Dispatchers.Default -> "Dispatchers.Default"
                Dispatchers.IO -> "Dispatchers.IO"
                Dispatchers.Main -> "Dispatchers.Main"
                else -> dispatcher.javaClass.simpleName
            }
        }

        fun createInfo(coroutineContext: CoroutineContext?):CoroutineInfo{
            return  if(coroutineContext != null){
                val hashCode = coroutineContext.hashCode()
                val coroutineName = coroutineContext[CoroutineName]?.name ?: "N/A"
                val dispatcherClass = coroutineContext[ContinuationInterceptor]?.javaClass?.simpleName ?: "UnknownDispatcher"
                val dispatcherName: String = dispatcherPrettyName(coroutineContext[ContinuationInterceptor])
                val job = coroutineContext[Job]
                val jobStatus = if (job?.isActive == true) "Active" else "Inactive or Completed"
                val childJobs = job?.children?.toList()?.size ?: 0

                val elementsDump = coroutineContext.fold(mutableListOf<String>()) { acc, element ->
                    val valueType = element::class.simpleName.toString()
                    acc.add(valueType)
                    acc
                }
                 CoroutineInfo(coroutineName, hashCode, dispatcherClass,dispatcherName,  jobStatus, childJobs, elementsDump)
            }else{
                 CoroutineInfo()
            }
        }
    }
}

fun CoroutineContext.coroutineInfo():CoroutineInfo{
   return CoroutineInfo.createInfo(this)
}

fun CoroutineScope.coroutineInfo():CoroutineInfo {
    return  CoroutineInfo.createInfo(coroutineContext)
}