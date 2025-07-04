package po.misc.data

import po.misc.data.console.DateHelper
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.interfaces.ComposableData
import po.misc.data.interfaces.Printable
import po.misc.data.json.JObject
import po.misc.data.json.JRecord
import po.misc.data.json.JsonHolder
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import kotlin.collections.listOf

abstract class PrintableBase<T>(
    var defaultTemplate: PrintableTemplateBase<T>
): ComposableData, Printable, DateHelper where T: Printable {

    abstract override val itemId :ValueBased
    abstract override val emitter: Identifiable
    abstract val self:T
    override var parentRecord: PrintableBase<*>? = null
    //protected val templateRegistry : BasicRegistry<T.() -> String> = BasicRegistry()
    override var children: List<PrintableBase<*>> = listOf()

    val templates: MutableList<PrintableTemplateBase<T>> = mutableListOf<PrintableTemplateBase<T>>().also {templateList->
        defaultTemplate.let {
            templateList.add(it)
        }
    }
    val formattedString : String get(){
        return defaultTemplate.resolve(self)
    }

    internal var jsonHolder : JsonHolder? = null
    internal val jsonObject : JObject get() {
        val itemIdRecord = JRecord("itemId", itemId.value)
        val emitterRecord = JRecord("emitter", emitter.completeName)
        val hostingClassName =  self::class.simpleName.toString()
        return JObject(hostingClassName).addRecord(itemIdRecord).addRecord(emitterRecord)
    }

    @PublishedApi
    internal var mute: Boolean = false
    @PublishedApi
    internal var muteCondition: ((T) -> Boolean)? = null
    var genericMuteCondition: ((ComposableData)-> Boolean)? = null

    private val templateNotFound : (Printable) -> String = { key-> "[template for data: $key not defined]"}

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

    internal fun changeDefaultTemplate(template: PrintableTemplateBase<T>){
        defaultTemplate = template
    }

    fun addTemplate(vararg template: PrintableTemplateBase<T>){
        template.forEach {
            templates.add(it)
        }
    }

    fun templatedString(template: PrintableTemplateBase<T>? = null): String{
        return template?.let {
            formatString(it.template)
        }?:run {
            formatString(defaultTemplate.template)
        }
    }

    fun echo(){
        if(!shouldMute()){
            //val result = templatedString()
            outputSource?.invoke(formattedString)?:run {
                println(formattedString)
            }
        }
    }
    fun echo(template: PrintableTemplateBase<T>){
        if(!shouldMute()){
            val result =formatString(template.template)
            outputSource?.invoke(result)?:run {
                println(result)
            }
        }
    }

    fun addChild(record: PrintableBase<*>){
        record.setParent(self as PrintableBase<*>)
        children = children.toMutableList().apply { add(record) }
    }

    fun addChildren(records:List<PrintableBase<*>>){
        records.map { it.setParent(self as PrintableBase<*>)}
        children = records
    }

    override fun setParent(parent: PrintableBase<*>) {
       parentRecord = parent
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

//    fun setTemplate(key: ValueBased, templateProvider: T.()-> String){
//        templateRegistry.addRecord(key, templateProvider)
//    }
    fun printTree(level: Int = 0, template: T.() -> String) {
        println("  ".repeat(level) + template(self))
        children.forEach {
            @Suppress("UNCHECKED_CAST")
            (it as? PrintableBase<T>)?.printTree(level + 1, template)
        }
    }

    override fun defaultsToJson(): JsonHolder? {
        if(parentRecord?.jsonHolder == null){
            val holder = JsonHolder()
            holder.addJsonObject(jsonObject)
            jsonHolder = holder
        }
        return jsonHolder
    }
}