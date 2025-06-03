package po.misc.data.console

import po.misc.interfaces.ValueBased
import po.misc.registries.basic.BasicRegistry


abstract class PrintableBase<T : PrintableBase<T>>(val printHelper: PrintHelper = PrintHelper.Companion): PrintHelper by printHelper {
    abstract val self:T
    protected val templateRegistry : BasicRegistry<()-> String> = BasicRegistry()

    protected var mute: Boolean = false
    private var muteCondition: ((T) -> Boolean)? = null

    private val templateNotFound : (Any) -> String = { key-> "[template for key: $key not defined]"}

    open fun print(message: String){
        if(mute) return
        muteCondition?.let {
           val shouldMute = it.invoke(self)
           if(!shouldMute){
               println(message)
           }
        }?:run {
            println(message)
        }
    }

    fun print(key: ValueBased){
        if(mute) return
        muteCondition?.let {
            val shouldMute = it.invoke(self)
            if(!shouldMute){
                println(templateRegistry.getRecord(key)?.invoke()?:templateNotFound(key))
            }
        }?:run {
            println(templateRegistry.getRecord(key)?.invoke()?:templateNotFound(key))
        }
    }

    fun setMute(condition:(T)-> Boolean){
        muteCondition = condition
    }

    fun printTable(headers: List<String>, rows: List<List<String>>) {
        val colWidths = headers.mapIndexed { i, header ->
            maxOf(header.length, rows.maxOfOrNull { it.getOrNull(i)?.length ?: 0 } ?: 0)
        }

        fun rowToString(row: List<String>): String =
            row.mapIndexed { i, cell -> cell.padEnd(colWidths[i]) }.joinToString(" | ")
        println(rowToString(headers))
        println(colWidths.joinToString("-+-") { "-".repeat(it) })
        rows.forEach { println(rowToString(it)) }
    }

    fun setTemplate(key: ValueBased, templateProvider:()-> String){
        templateRegistry.addRecord(key, templateProvider)
    }

}