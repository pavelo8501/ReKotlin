package po.misc.data.console

import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableProvider

interface PrintableWithTemplate : Printable {
    fun defaultTemplate(): String = this.toString()
}

sealed class PrintableTemplateBase<T: Printable>: PrintableProvider<T>{
    abstract val templateName: String
    abstract val templateParts: List<T.() -> String>
    abstract override val template: T.() -> String
    internal fun resolve(receiver: T): String = template(receiver)
}


class PrintableTemplate<T: Printable>:PrintableTemplateBase<T>{

    override var templateName: String

    override val templateParts: List<T.() -> String>
    override val template: T.() -> String

     constructor(templateName: String, delimiter: String, vararg templateParts: T.() -> String) {
        this.templateName = templateName
        this.templateParts = templateParts.toList()
        this.template = {
            templateParts.joinToString(delimiter) { it(this) }
        }
    }
    constructor(templateName: String, template: T.() -> String = {(this as? PrintableWithTemplate)?.defaultTemplate() ?: toString() }) {
        this.templateName = templateName
        this.templateParts = listOf(template)
        this.template = template
    }

}


class DebugTemplate<T: Printable>:PrintableTemplateBase<T>{

    override val templateName: String
        get() = "DebugTemplate"

    override val templateParts: List<T.() -> String>
    override val template: T.() -> String
    constructor(vararg template: T.() -> String, delimiter: String = "\n") {
        this.templateParts = template.toList()
        this.template = {
            templateParts.joinToString(delimiter) { it(this) }
        }
    }
    constructor(template: T.() -> String = {(this as? PrintableWithTemplate)?.defaultTemplate() ?: toString() }) {
        this.templateParts = listOf(template)
        this.template = template
    }
}

