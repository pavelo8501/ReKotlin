package po.misc.data.console

import po.misc.data.printable.Printable

interface PrintableWithTemplate : Printable {
    fun defaultTemplate(): String = this.toString()
}


//interface PrintableProvider<T:Printable> {
//    val template: T.(TemplateAuxParams)-> String
//}

sealed class PrintableTemplateBase<T: Printable>{
    abstract val templateName: String
    abstract val templateParts: List<T.(TemplateAuxParams) -> String>
    abstract val template: T.(TemplateAuxParams) -> String
    var params: TemplateAuxParams? = null

    internal fun resolve(receiver: T): String {
       return template.invoke(receiver, getOrCreateParams())
    }

    fun getOrCreateParams():TemplateAuxParams{
        return params?:TemplateAuxParams()
    }

    fun setAuxParams(auxiliaryParameters: TemplateAuxParams){
        params = auxiliaryParameters
    }

}


class PrintableTemplate<T: Printable>:PrintableTemplateBase<T>{

    override var templateName: String

    override val templateParts: List<T.(TemplateAuxParams) -> String>
    override val template: T.(TemplateAuxParams) -> String

    constructor(templateName: String, delimiter: String, vararg templateParts: T.(TemplateAuxParams) -> String) {
        this.templateName = templateName
        this.templateParts = templateParts.toList()
        this.template = {
            templateParts.joinToString(delimiter) { it(this, getOrCreateParams()) }
        }
    }
    constructor(templateName: String, template: T.(TemplateAuxParams) -> String = {(this as? PrintableWithTemplate)?.defaultTemplate() ?: toString() }) {
        this.templateName = templateName
        this.templateParts = listOf(template)
        this.template = template
    }


}

data class TemplateAuxParams(
    val prefix: String? = null
)


//class DebugTemplate<T: Printable>:PrintableTemplateBase<T>{
//
//    override val templateName: String
//        get() = "DebugTemplate"
//
//    override val templateParts: List<T.() -> String>
//    override val template: T.() -> String
//    constructor(vararg template: T.() -> String, delimiter: String = "\n") {
//        this.templateParts = template.toList()
//        this.template = {
//            templateParts.joinToString(delimiter) { it(this) }
//        }
//    }
//    constructor(template: T.() -> String = {(this as? PrintableWithTemplate)?.defaultTemplate() ?: toString() }) {
//        this.templateParts = listOf(template)
//        this.template = template
//    }
//}

