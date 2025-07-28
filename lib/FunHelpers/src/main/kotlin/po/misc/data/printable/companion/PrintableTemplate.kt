package po.misc.data.printable.companion

import po.misc.data.printable.Printable
import po.misc.data.styles.SpecialChars
import po.misc.functions.dsl.ConstructableDSL
import po.misc.functions.dsl.DSLConstructor
import po.misc.functions.dsl.helpers.dslConstructor

interface PrintableWithTemplate : Printable {
    fun defaultTemplate(): String = this.toString()
}

sealed class PrintableTemplateBase<T: Printable>() {

    var templateParts: List<T.() -> String> = listOf()
    var params: TemplateAuxParams? = null

    var isDefaultTemplate: Boolean = false

    abstract fun evaluateTemplate(data: T): String

    internal fun resolve(receiver: T): String {
        return  evaluateTemplate(receiver)
    }
    fun setAuxParams(auxiliaryParameters: TemplateAuxParams){
        params = auxiliaryParameters
    }
}

@Deprecated("Will get depreciated in favour of Dsl powered template", ReplaceWith("dslBuilder"), DeprecationLevel.WARNING)
class PrintableTemplate<T: Printable>(
    val template: T.() -> String = { (this as? PrintableWithTemplate)?.defaultTemplate() ?: toString()  }
):PrintableTemplateBase<T>(){

    override fun evaluateTemplate(data: T): String {
        return template.invoke(data)
    }
}

class PartsTemplate<T: Printable>(
    val delimiter: String,
    vararg templateParts: T.() -> String
):PrintableTemplateBase<T>(){

    override fun evaluateTemplate(data: T): String {
        return templateParts.joinToString(delimiter) { it(data) }
    }
}

class Template<T: Printable>():PrintableTemplateBase<T>(), ConstructableDSL<T, String> {

    var delimiter: String = SpecialChars.NewLine.char

    internal val dslConstructor: DSLConstructor<T, String> = dslConstructor()

    override fun evaluateTemplate(data: T): String {
        return  dslConstructor.resolve(data){stringList->
            stringList.joinToString(separator = delimiter) {string-> string }
        }
    }
}



data class TemplateAuxParams(
    val prefix: String? = null
)
