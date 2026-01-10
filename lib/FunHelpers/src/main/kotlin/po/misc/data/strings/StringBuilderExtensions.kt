package po.misc.data.strings

import po.misc.collections.toList
import po.misc.data.helpers.firstCharUppercase
import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.toDisplayName
import po.misc.debugging.ClassResolver
import po.misc.reflection.displayName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

fun StringBuilder.className(kClass: KClass<*>): StringBuilder{
    val className = ClassResolver.classInfo(kClass).simpleName
    return appendLine(className)
}


fun StringBuilder.appendParam(parameterName: String, value: Any?): StringBuilder{
    if(value != null){
        val string = "$parameterName: $value "
        return append(string)
    }else{
        return this
    }
}

fun StringBuilder.appendLineParam(parameterName: String, value: Any?): StringBuilder{
    if(value != null){
        val string = "$parameterName: $value"
        return appendLine(string)
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
