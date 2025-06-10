package po.misc.data.console

import po.misc.data.interfaces.Printable
import po.misc.data.interfaces.PrintableProvider

interface PrintableWithTemplate : Printable {
    fun defaultTemplate(): String = this.toString()
}

sealed class PrintableTemplateBase<T: Printable>:PrintableProvider<T>{

    abstract val templateParts: List<T.() -> String>
    abstract override val template: T.() -> String
    internal fun resolve(receiver: T): String = template(receiver)
}


class PrintableTemplate<T: Printable>:PrintableTemplateBase<T>{
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


class DebugTemplate<T: Printable>:PrintableTemplateBase<T>{
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

