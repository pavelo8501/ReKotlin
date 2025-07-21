package po.misc.data.printable

import po.misc.data.console.DateHelper
import po.misc.data.json.JObject
import po.misc.data.json.JRecord
import po.misc.data.json.JsonHolder
import po.misc.context.CTX

abstract class PrintableBase<T>(
    var defaultTemplate: PrintableTemplateBase<T>
): ComposableData, Printable, DateHelper where T:PrintableBase<T> {

    abstract val self:T
    override var parentRecord: PrintableBase<*>? = null
    override var children: List<PrintableBase<*>> = listOf()

    var activeTemplate:  PrintableTemplateBase<T>? = null
    val templates: MutableList<PrintableTemplateBase<T>> = mutableListOf<PrintableTemplateBase<T>>()

    override val formattedString : String get(){
        return activeTemplate?.resolve(self) ?:run {
            defaultTemplate.resolve(self)
        }
    }

    internal var jsonHolder : JsonHolder? = null
    internal val jsonObject : JObject
        get() {
        val hostingClassName =  self::class.simpleName.toString()
        return JObject(hostingClassName)
    }

    @PublishedApi
    internal var mute: Boolean = false
    @PublishedApi
    internal var muteCondition: ((T) -> Boolean)? = null
    var genericMuteCondition: ((ComposableData)-> Boolean)? = null

    private val templateNotFound : (Printable) -> String = { key-> "[template for data: $key not defined]"}

    @PublishedApi
    internal var outputSource: ((String)-> Unit)?=null

    init {
        activeTemplate?:run {
            activeTemplate = defaultTemplate
        }
    }

    private fun formatString(stringProvider: T.()-> String): String{
       return stringProvider.invoke(self)
    }

    private fun formatString(template: PrintableTemplateBase<T>): String{
        return template.evaluateTemplate(self)
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

    override fun setParent(parent: PrintableBase<*>) {
        parentRecord = parent
    }

    fun addTemplate(vararg template: PrintableTemplateBase<T>){
        template.forEach {
            templates.add(it)
        }
    }

    fun templatedString(template: PrintableTemplateBase<T>): String{
        return template.resolve(self)
    }

    override fun echo(){
        if(!shouldMute()) {
            outputSource?.invoke(formattedString) ?: run {
                println(formattedString)
            }
        }
    }
    fun echo(template: PrintableTemplateBase<T>){
        if(!shouldMute()){
            val result = template.resolve(self)
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

    fun setGenericMute(condition:(ComposableData)-> Boolean){
        genericMuteCondition = condition
    }

    fun setMute(condition:((T)-> Boolean)? = null ){
        muteCondition = condition
    }

    fun printTree(level: Int = 0, template: T .() -> String) {
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