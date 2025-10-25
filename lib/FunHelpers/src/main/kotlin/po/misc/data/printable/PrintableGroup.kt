package po.misc.data.printable

import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.data.styles.SpecialChars
import kotlin.reflect.KClass


abstract class PrintableGroup<T1: PrintableBase<T1>, T2:  PrintableBase<T2>>(
    val groupHost:T1,
    var hostDefaultTemplate: PrintableTemplateBase<T1>,
    var childrenDefaultTemplate: PrintableTemplateBase<T2>
): Printable{

    override val formattedString: String get() {
       return createFormatedString()
    }

   // val arbitraryMap: ArbitraryDataMap<Printable> = groupHost.arbitraryMap
  //  override val ownClass: KClass<out Printable> = groupHost.ownClass

    var headerTemplate: PrintableTemplateBase<T1> = hostDefaultTemplate
    var footerTemplate: PrintableTemplateBase<T1>? = null

    private val recordsBacking: MutableList<T2> = mutableListOf()
    val records: List<T2> = recordsBacking

    var childProcessLambda: (T2.(PrintableGroup<T1, T2>)-> Unit)? = null

    fun processChild(block:T2.(PrintableGroup<T1, T2>)-> Unit){
        childProcessLambda = block
    }

    private var newChildCallback : (T2.()-> Unit)? = null
    fun onNewRecord(callback:T2.()-> Unit){
        newChildCallback = callback
    }

    fun finalize(block:T1.()-> Unit){
        groupHost.block()
    }

    private fun createFormatedString(): String {
        val result: MutableList<String> = mutableListOf()
        result.add(headerTemplate.resolve(groupHost))
        val childStrings = recordsBacking.map { childrenDefaultTemplate.resolve(it) }
        result.addAll(childStrings)
        result.add(footerTemplate?.resolve(groupHost) ?: "")
        return result.joinToString(separator = SpecialChars.NEW_LINE) { it }
    }

    fun setHeader(header: PrintableTemplateBase<T1>){
        headerTemplate  = header
    }

    fun setFooter(footer: PrintableTemplateBase<T1>){
        footerTemplate = footer
    }

    fun addRecord(printable: T2):PrintableGroup<T1,T2>{
        newChildCallback?.invoke(printable)
        recordsBacking.add(printable)
        return  this
    }

    fun clear(): Unit = recordsBacking.clear()

    fun echo() {
        println(formattedString)
    }

}