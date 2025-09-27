package po.misc.data.printable

import po.misc.data.PrettyPrint
import po.misc.data.console.DateHelper
import po.misc.data.helpers.output
import po.misc.data.json.JObject
import po.misc.data.json.JsonHolder
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.data.printable.grouping.ArbitraryKey
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.types.helpers.simpleOrNan
import po.misc.types.safeCast
import kotlin.reflect.KClass

abstract class PrintableBase<T>(
   val companion: PrintableCompanion<T>
): ComposableData, Printable, DateHelper, PrettyPrint where T:PrintableBase<T> {

    private val templateNotFound : (T) -> String = { key->
        "[template for data: $key not defined] ${companion.printableClass.simpleOrNan()}".colorize(Colour.Red)
    }

    abstract val self:T
    override val arbitraryMap: ArbitraryDataMap<PrintableBase<*>> = ArbitraryDataMap()
    val arbitraryRecordsFlattened: List<PrintableBase<*>> get() = arbitraryMap.flatMap { it.value }

    var parentRecord:PrintableBase<*>? = null

    var activeTemplate:  PrintableTemplateBase<T>? = null
    private val templatesBacking: MutableList<PrintableTemplateBase<T>> = mutableListOf<PrintableTemplateBase<T>>()
    val templates :List<PrintableTemplateBase<T>> = templatesBacking
    override val formattedString : String get(){
        return activeTemplate?.resolve(self) ?:run {
            templateNotFound(self)
        }
    }

    val ownClass: KClass<T> get() = companion.printableClass

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
    internal var outputSource: ((String)-> Unit)? = null

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

    fun trySetDefaultTemplate(template: PrintableTemplateBase<*>){
        val casted = template.safeCast<PrintableTemplateBase<T>>()
        if(casted != null){
            activeTemplate = casted
        }
    }

    override fun echo(): Unit = output()

    fun echo(template: PrintableTemplateBase<T>){
        val result = template.resolve(self)
        println(result)
    }

    override fun setParent(parent: PrintableBase<*>): PrintableBase<*> {
        parentRecord = parent
        return this
    }

    fun addArbitraryRecord(record: PrintableBase<*>): ArbitraryKey {
       record.setParent(this)
       return arbitraryMap.putPrintable(record)
    }

    fun addArbitraryRecord(record: PrintableBase<*>, prefixBuilder:(PrintableBase<*>)-> String): ArbitraryKey{
        record.setParent(this)
       return arbitraryMap.putPrintable(record, prefixBuilder)
    }

    fun <T2: PrintableBase<T2>> addArbitraryRecords(records: List<T2>): ArbitraryKey?{
        if (records.isEmpty()) return null
        val key = arbitraryMap.putPrintable(records[0].setParent(this))
        if (records.size > 1) {
            records.subList(1, records.size).forEach { arbitraryMap.putPrintable(it.setParent(this)) }
        }
        return key
    }

    fun setGenericMute(condition:(ComposableData)-> Boolean){
        genericMuteCondition = condition
    }

    fun setMute(condition:((T)-> Boolean)? = null ){
        muteCondition = condition
    }

}