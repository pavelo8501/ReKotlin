package po.misc.data.printable

import po.misc.data.PrettyPrint
import po.misc.data.json.JObject
import po.misc.data.json.JsonHolder
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.data.printable.grouping.ArbitraryKey
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.time.TimeHelper
import po.misc.types.safeCast
import kotlin.reflect.KClass



abstract class PrintableBase<T>(
   val companion: PrintableCompanion<T>
): ComposableData<T>, Printable, TimeHelper, PrettyPrint where T:Printable {

    data class Config(
        var explicitOutput: Boolean = false
    )
    val printableConfig: Config = Config()
    private val templateNotFound : (T) -> String = { key->
        "[template for data: $key not defined. Using toString()] ${companion.typeToken.simpleName}".colorize(Colour.Yellow)
    }
    abstract val self:T

    override val arbitraryMap  = ArbitraryDataMap<T>()

    var parentRecord:Printable? = null
    var activeTemplate:  PrintableTemplateBase<T>? = null
    private val templatesBacking: MutableList<PrintableTemplateBase<T>> = mutableListOf<PrintableTemplateBase<T>>()
    val templates :List<PrintableTemplateBase<T>> = templatesBacking
    override val formattedString : String get(){
        return activeTemplate?.resolve(self) ?:run {
            templateNotFound(self)
            toString()
        }
    }
    val ownClass: KClass<T> get() = companion.typeToken.kClass

    internal var jsonHolder : JsonHolder? = null
    internal val jsonObject : JObject
        get() {
        val hostingClassName = companion.typeToken.simpleName
        return JObject(hostingClassName)
    }

    @PublishedApi
    internal var mute: Boolean = false
    @PublishedApi
    internal var muteCondition: ((T) -> Boolean)? = null
    var genericMuteCondition: ((PrintableBase<*>)-> Boolean)? = null

    init {
        templatesBacking.addAll(companion.templates)
        companion.templates.firstOrNull { it.isDefaultTemplate }?.let {
            activeTemplate = it
        }?:run {
            activeTemplate = companion.templates.firstOrNull()
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

    fun echo(): Unit{
        if(!printableConfig.explicitOutput){
            println(formattedString)
        }
    }

    fun echo(template: PrintableTemplateBase<T>){
        val result = template.resolve(self)
        println(result)
    }

    override fun setParent(parent: Printable):Printable {
        parentRecord = parent
        return this
    }

    fun addArbitraryRecord(record: PrintableBase<*>): ArbitraryKey {
       record.setParent(this)
       return arbitraryMap.putPrintable(record)
    }

    fun muteCondition(condition:((T)-> Boolean)? = null ){
        muteCondition = condition
    }

}