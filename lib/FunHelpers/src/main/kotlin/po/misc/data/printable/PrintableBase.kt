package po.misc.data.printable

import po.misc.data.console.DateHelper
import po.misc.data.json.JObject
import po.misc.data.json.JsonHolder
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase

abstract class PrintableBase<T>(
    private val companion: PrintableCompanion<T>
): ComposableData, Printable, DateHelper where T:PrintableBase<T> {


    private val templateNotFound : (T) -> String = { key->  "[template for data: $key not defined] ${toString()}"}

    abstract val self:T
    override var parentRecord: PrintableBase<*>? = null
    override var children: List<PrintableBase<*>> = listOf()

    var activeTemplate:  PrintableTemplateBase<T>? = null
    private val templatesBacking: MutableList<PrintableTemplateBase<T>> = mutableListOf<PrintableTemplateBase<T>>()
    val templates :List<PrintableTemplateBase<T>> = templatesBacking
    override val formattedString : String get(){
        return activeTemplate?.resolve(self) ?:run {
            templateNotFound(self)
        }
    }

    internal var jsonHolder : JsonHolder? = null
    internal val jsonObject : JObject
        get() {
        val hostingClassName = companion.printableClass.simpleName.toString()
        return JObject(hostingClassName)
    }

    @PublishedApi
    internal var mute: Boolean = false
    @PublishedApi
    internal var muteCondition: ((T) -> Boolean)? = null
    var genericMuteCondition: ((ComposableData)-> Boolean)? = null



    @PublishedApi
    internal var outputSource: ((String)-> Unit)?=null

    init {
        templatesBacking.addAll(companion.templates)
        val defaultTemplate = companion.templates.firstOrNull { it.isDefaultTemplate }
        if(defaultTemplate != null){
            activeTemplate = defaultTemplate
        }else{
            activeTemplate =  companion.templates.firstOrNull()
        }
    }

    private fun shouldMute(): Boolean{
        if(mute) return true
        if(genericMuteCondition?.invoke(this)?:false){
            return true
        }
       return muteCondition?.invoke(self)?:false
    }

    fun setDefaultTemplate(template: PrintableTemplateBase<T>){
        activeTemplate = template
    }

    override fun setParent(parent: PrintableBase<*>) {
        parentRecord = parent
    }

    override fun echo(){
        outputSource?.invoke(formattedString) ?: run {
            println(formattedString)
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