package po.misc.data.printable

import po.misc.functions.dsl.DSLBuilder
import po.misc.functions.dsl.DSLContainer

interface PrintableWithTemplate : Printable {
    fun defaultTemplate(): String = this.toString()
}



sealed class PrintableTemplateBase<T: Printable>() {

    var templateParts: List<T.() -> String> = listOf()
    var params: TemplateAuxParams? = null

    protected fun provideTemplateParts(parts: List<T.() -> String>) {
        templateParts = parts
    }

    abstract fun evaluateTemplate(data: T): String

    internal fun resolve(receiver: T): String {
        return  evaluateTemplate(receiver)
    }

    fun getOrCreateParams():TemplateAuxParams{
        return params?:TemplateAuxParams()
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

class Template<T: Printable>(val delimiter: String):PrintableTemplateBase<T>(), DSLBuilder<T, String> {

    override val dslContainer: DSLContainer<T, String> = DSLContainer()

    override fun evaluateTemplate(data: T): String {
      return  dslContainer.resolve(data){
            it.joinToString(separator = delimiter) {string-> string }
        }
    }
}

data class TemplateAuxParams(
    val prefix: String? = null
)
