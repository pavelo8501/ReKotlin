package po.lognotify.process

import kotlinx.coroutines.CoroutineName
import po.misc.context.CTX
import po.misc.data.logging.LogCollector
import po.misc.types.TypeData
import java.util.UUID
import kotlin.coroutines.CoroutineContext


class ProcessKey<T>(
    val coroutineName: CoroutineName,
    val typeData: TypeData<Process<T>>
) : Comparable<ProcessKey<*>> where T: CTX, T: LogCollector, T: CoroutineContext.Element {

    val processName: String = "Process#${UUID.randomUUID()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessKey<*>) return false
        return typeData == other.typeData
    }

    override fun hashCode(): Int = typeData.hashCode()

    override fun compareTo(other: ProcessKey<*>): Int =
        compareValuesBy(this, other, { it.processName })

    override fun toString(): String = "ProcessKey<${processName}>"

    companion object{
        inline fun <reified T> create(
            coroutineName: CoroutineName,
        ): ProcessKey<T> where  T: CTX, T: LogCollector, T: CoroutineContext.Element{
           val typeData = TypeData.create<Process<T>>()
           return ProcessKey(coroutineName, typeData)
        }
    }
}