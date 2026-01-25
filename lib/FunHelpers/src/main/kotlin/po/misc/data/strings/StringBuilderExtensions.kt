package po.misc.data.strings

import po.misc.collections.flattenVarargs
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.toDisplayName
import po.misc.reflection.displayName
import kotlin.reflect.KProperty0




/**
 * Appends a single value followed by a line break, applying optional styling.
 *
 * The [parameter] is converted using [TextStyler.formatKnownTypes], which defines
 * how different value types (e.g. Boolean, Number, String) are rendered.
 *
 * If [styleCode] is provided, the plain (unstyled) representation is styled and
 * then appended. Otherwise, the formatted representation is appended as-is.
 *
 * This function does not interfere with standard [StringBuilder.appendLine] and
 * is intended for explicit styled output.
 *
 * @param parameter value to append
 * @param styleCode optional style to apply to the value
 * @return this [StringBuilder] for chaining
 */
fun StringBuilder.appendStyledLine(parameter: Any, styleCode: StyleCode? = null): StringBuilder{
    val formatted = TextStyler.formatKnownTypes(parameter)
    val text = styleCode
        ?.let { TextStyler.style(formatted.plain, it) }
        ?: formatted.styled
    return appendLine(text)
}

/**
 * Appends multiple values without a line break, preserving each value's
 * individual formatting.
 *
 * All [parameters] values are flattened before rendering, allowing lists
 * (and other grouped arguments) to be passed inside the vararg for easier
 * spacing and composition.
 *
 * Each value is converted using [TextStyler.formatKnownTypes] and appended
 * in sequence without additional separators.
 *
 * Example:
 * ```
 * appendStyled("Value: ", true, " ")
 * appendStyled(listOf("(", 42, ")"))
 * ```
 *
 * @param parameters values to append
 * @return this [StringBuilder] for chaining
 */
fun StringBuilder.appendStyled(vararg parameters: Any): StringBuilder{
    val flattened = parameters.flattenVarargs()
    val text = flattened.joinToString(separator = "") {
        TextStyler.formatKnownTypes(it).styled
    }
    return append(text)
}

/**
 * Appends multiple values without a line break, applying a single style to all of them.
 *
 * All [parameters] values are flattened before rendering, allowing nested collections
 * to be passed to the vararg for convenient grouping and spacing.
 *
 * Each value is first converted using [TextStyler.formatKnownTypes], then the
 * plain representation is styled using the provided [styleCode].
 *
 * This is useful when multiple values should share the same visual style.
 *
 * Example:
 * ```
 * appendStyled(Colour.Blue, "Result: ", true)
 * ```
 *
 * @param styleCode style applied to all values
 * @param parameters values to append
 * @return this [StringBuilder] for chaining
 */
fun StringBuilder.appendStyled(styleCode: StyleCode, vararg parameters: Any): StringBuilder{
    val flattened = parameters.flattenVarargs()
    val text = flattened.joinToString(separator = "") {
        TextStyler.style(TextStyler.formatKnownTypes(it).plain,styleCode)
    }
    return append(text)
}

fun StringBuilder.appendParam(parameterName: String, value: Any?): StringBuilder{
    if(value != null){
        val string = "$parameterName: $value "
        return append(string)
    }else{
        return this
    }
}


fun StringBuilder.appendParam(vararg props: KProperty0<*>): StringBuilder{
    val propStr = props.toList().joinToString() {property->
        "${property.displayName}: ${property.get().toString()}"
    }
    append("$propStr ")
    return this
}

fun StringBuilder.appendLine(properties: List<KProperty0<*>>): StringBuilder{
    val propStr = properties.joinToString(separator = SpecialChars.NEW_LINE) {property->
        "${property.displayName}: ${property.get().toString()}"
    }
    append(propStr)
    return this
}

fun StringBuilder.appendLineParam(vararg  props: KProperty0<*>): StringBuilder = appendLine(props.toList())

fun StringBuilder.appendGroup(prefix: String, postfix: String = "", vararg props: KProperty0<*>):StringBuilder{

    val propStr = props.toList().joinToString(separator= ", ") {property->
        val name = property.name.toDisplayName()
        val value =  property.get()
        val formated = TextStyler.formatKnownTypes(value)
        "${name}: $formated"
    }
    return append("$prefix$propStr$postfix ")
}

fun StringBuilder.appendVertical(header: String, footer: String,  vararg props: KProperty0<*>):StringBuilder{
    val propStr = props.toList().joinToString(separator = SpecialChars.NEW_LINE) { property->
        val name = property.name.toDisplayName()
        val value =  property.get()
        val formated = TextStyler.formatKnownTypes(value)
        "${name}: $formated "
    }
    appendLine("$header $propStr $footer?:")
    return this
}

fun StringBuilder.appendVertical(header: String, vararg props: KProperty0<*>):StringBuilder{
    val propStr = props.toList().joinToString(separator = SpecialChars.NEW_LINE) { property->
        val name = property.name.toDisplayName()
        val value =  property.get()
        val formated = TextStyler.formatKnownTypes(value)
        "${name}: $formated "
    }
    appendLine("$header $propStr")
    return this
}
