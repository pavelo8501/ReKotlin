package po.misc.data.console

import po.misc.interfaces.ValueBased
import po.misc.registries.basic.BasicRegistry



abstract class PrintableBase<T>():PrintHelper where T: PrintableBase<T> {

    abstract val self:T
    protected val templateRegistry : BasicRegistry<T.()-> String> = BasicRegistry()

    @PublishedApi
    internal var mute: Boolean = false
    @PublishedApi
    internal var muteCondition: ((T) -> Boolean)? = null

    private val templateNotFound : (Any) -> String = { key-> "[template for key: $key not defined]"}

    private fun formatString(stringProvider: T.()-> String): String{
       return stringProvider.invoke(self)
    }

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
        val shouldMute = muteCondition?.invoke(self)?:false
        if(!shouldMute){
            val message = formatString(templateRegistry.getRecord(key)?:templateNotFound)
            println(message)
        }
    }

    fun print(template: PrintableTemplate<T>){
        if(mute) return
        val shouldMute = muteCondition?.invoke(self)?:false
        if(!shouldMute){
            println(formatString(template.template))
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

    fun setTemplate(key: ValueBased, templateProvider:T.()-> String){
        templateRegistry.addRecord(key, templateProvider)
    }

}