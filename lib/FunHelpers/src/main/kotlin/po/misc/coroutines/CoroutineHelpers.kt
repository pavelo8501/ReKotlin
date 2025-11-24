package po.misc.coroutines


import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import po.misc.types.k_class.simpleOrAnon
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass


data class CoroutineInfo(
    val coroutineName: String = "Context Unavailable",
    val hashCode: Int = -1,
    val dispatcherClass: String = "Context Unavailable",
    val dispatcherName: String = "Context Unavailable",
    val jobStatus: String = "N/A",
    val childJobCount: Int = -1,
    val topLevelKeys : List<String> = emptyList()
){

    var inClassName: String? = null
    var inMethodName: String? = null

    fun setClassNameMethod(name: String, methodName: String):CoroutineInfo{
        inClassName = name
        inMethodName = methodName
        return this
    }
    override fun toString(): String {
        val classInfo = "[$inClassName Method name : $inMethodName]"
        val keys = topLevelKeys.joinToString(prefix = "Top level keys:[", separator = "/", postfix = "]") { it }
        return buildString {
            if(inClassName != null && inMethodName != null){
                appendLine(classInfo)
            }
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

    companion object {

        private fun dispatcherPrettyName(dispatcher: ContinuationInterceptor?): String {
            if (dispatcher == null) return "Unknown"
            return when (dispatcher) {
                Dispatchers.Default -> "Dispatchers.Default"
                Dispatchers.IO -> "Dispatchers.IO"
                Dispatchers.Main -> "Dispatchers.Main"
                else -> dispatcher.javaClass.simpleName
            }
        }

        fun createInfo(coroutineContext: CoroutineContext): CoroutineInfo {
            val hashCode = coroutineContext.hashCode()
            val coroutineName = coroutineContext[CoroutineName]?.name ?: "N/A"
            val dispatcherClass =
                coroutineContext[ContinuationInterceptor]?.javaClass?.simpleName ?: "UnknownDispatcher"
            val dispatcherName: String = dispatcherPrettyName(coroutineContext[ContinuationInterceptor])
            val job = coroutineContext[Job]
            val jobStatus = if (job?.isActive == true) "Active" else "Inactive or Completed"
            val childJobs = job?.children?.toList()?.size ?: 0

            val elementsDump = coroutineContext.fold(mutableListOf<String>()) { acc, element ->
                val valueType = element::class.simpleName.toString()
                acc.add(valueType)
                acc
            }
            return CoroutineInfo(
                coroutineName,
                hashCode,
                dispatcherClass,
                dispatcherName,
                jobStatus,
                childJobs,
                elementsDump
            )
        }
    }

}

fun CoroutineContext.coroutineInfo():CoroutineInfo{
   return CoroutineInfo.createInfo(this)
}
fun CoroutineScope.coroutineInfo():CoroutineInfo = coroutineContext.coroutineInfo()


fun CoroutineContext.coroutineInfo(inClass: KClass<*>, methodName: String):CoroutineInfo{
    val info = CoroutineInfo.createInfo(this)
    return info.setClassNameMethod(inClass.simpleOrAnon, methodName)
}



fun CoroutineScope.coroutineInfo(inClass: KClass<*>, methodName: String):CoroutineInfo =
    coroutineContext.coroutineInfo(inClass, methodName)

suspend inline fun KClass<*>.coroutineInfo(methodName: String): CoroutineInfo{
   return currentCoroutineContext().coroutineInfo(this, methodName)
}