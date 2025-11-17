package po.misc.data.printable.companion

import po.misc.data.printable.Printable
import po.misc.data.styles.SpecialChars
import po.misc.functions.dsl.ConstructableDSL
import po.misc.functions.dsl.DSLConstructor
import po.misc.functions.dsl.helpers.dslConstructor


sealed class PrintableTemplateBase<T: Printable>() {
    var templateParts: List<T.() -> String> = listOf()
    var isDefaultTemplate: Boolean = false

    abstract fun evaluateTemplate(data: T): String
    internal fun resolve(receiver: T): String {
        return  evaluateTemplate(receiver)
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
    var delimiter: String = SpecialChars.NEW_LINE
    internal val dslConstructor: DSLConstructor<T, String> = dslConstructor()

    override fun evaluateTemplate(data: T): String {
        return dslConstructor.resolve(data) { stringList ->
            stringList.joinToString(separator = delimiter) { string -> string }
        }
    }
}
