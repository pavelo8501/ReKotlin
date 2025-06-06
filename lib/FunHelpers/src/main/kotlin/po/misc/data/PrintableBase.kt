package po.misc.data

import po.misc.data.console.PrintHelper
import po.misc.data.console.PrintableTemplate
import po.misc.data.interfaces.ComposableData
import po.misc.data.interfaces.Printable
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.registries.basic.BasicRegistry

abstract class PrintableBase<T>()
    : ComposableData, Printable, PrintHelper where T: Printable
{

    abstract override val itemId :ValueBased
    abstract override val emitter: Identifiable
    abstract val self:T
    override var parentRecord: T? = null

    protected val templateRegistry : BasicRegistry<T.() -> String> = BasicRegistry()
    override var children: List<ComposableData> = listOf()


    @PublishedApi
    internal var mute: Boolean = false
    @PublishedApi
    internal var muteCondition: ((T) -> Boolean)? = null
    var genericMuteCondition: ((ComposableData)-> Boolean)? = null

    private val templateNotFound : (Any) -> String = { key-> "[template for key: $key not defined]"}

    @PublishedApi
    internal var outputSource: ((String)-> Unit)?=null

    private fun formatString(stringProvider: T.()-> String): String{
       return stringProvider.invoke(self)
    }
    private fun shouldMute(): Boolean{
        if(mute) return true
        if(genericMuteCondition?.invoke(this)?:false){
            return true
        }
       return muteCondition?.invoke(self)?:false
    }
    private fun outputData(data: String){
        outputSource?.invoke(data) ?:run {
            println(data)
        }
    }

    fun addChildren(records:List<ComposableData>){
        records.map { it.setParent(self)}
        children = records
    }
    override fun setParent(parent: Printable) {
       parentRecord = parent as T
    }

    fun print(): String?{
        if(!shouldMute()){
            val result = toString()
            outputData(result)
            return result
        }
        return null
    }

    fun print(message: String? = null): String?{
        if(!shouldMute()){
           val result =  message?: toString()
            outputData(result)
            return result

        }
        return null
    }

    fun print(key: ValueBased): String?{
        if(!shouldMute()){
            val result =  formatString(templateRegistry.getRecord(key)?:templateNotFound)
            outputData(result)
            return result
        }
        return null
    }

    fun printTemplate(template: PrintableTemplate<T>): String?{
        if(!shouldMute()){
            val result =formatString(template.template)
            outputData(result)
            return result
        }
        return null
    }

    fun setGenericMute(condition:(ComposableData)-> Boolean){
        genericMuteCondition = condition
    }
    fun setMute(condition:((T)-> Boolean)? = null ){
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

    fun setTemplate(key: ValueBased, templateProvider: T.()-> String){
        templateRegistry.addRecord(key, templateProvider)
    }
    fun printTree(level: Int = 0, template: T.() -> String) {
        println("  ".repeat(level) + template(self))
        children.forEach {
            if (it is PrintableBase<*>) {
                @Suppress("UNCHECKED_CAST")
                (it as? PrintableBase<T>)?.printTree(level + 1, template)
            }
        }
    }


}