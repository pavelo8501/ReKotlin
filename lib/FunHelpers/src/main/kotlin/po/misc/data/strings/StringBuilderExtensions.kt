package po.misc.data.strings

import po.misc.data.helpers.firstCharUppercase
import po.misc.data.helpers.orDefault
import po.misc.data.styles.SpecialChars
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

fun StringBuilder.appendLine(firstString: String,  vararg strings: String): StringBuilder{
    val result = mutableListOf<String>(firstString)
    val varargStrings = strings.toList()
    result.addAll(varargStrings)
    append(result.joinToString(SpecialChars.NEW_LINE))
    return this
}

fun StringBuilder.append(firstString: String, vararg strings: String): StringBuilder{
    val result = mutableListOf<String>(firstString)
    val varargStrings = strings.toList()
    result.addAll(varargStrings)
    append(result.joinToString())
    return this
}

fun StringBuilder.appendParam(parameterName: String, value: Any?): StringBuilder{
    if(value != null){
        val string = "$parameterName: $value"
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
    append(propStr)
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


fun StringBuilder.appendVertical(header: String, vararg props: KProperty0<*>, footer: String? = null):StringBuilder{
    val lines = mutableListOf<String>()
    lines.add(header.firstCharUppercase())
    val propStr = props.toList().joinToString(separator = SpecialChars.NEW_LINE) {property->
        "${property.name}: ${property.get().toString()}"
    }
    lines.add(propStr)
    if(footer != null){
        lines.add(footer)
    }
    val text = lines.joinToString(separator = SpecialChars.NEW_LINE)
    append(text)
    return this
}


fun StringBuilder.appendGroup(prefix: String, postfix: String, vararg props: KProperty0<*>):StringBuilder{
    val propStr = props.toList().joinToString {property->
        val name = property.name?:"N/A"
        val value = property.get().toString()?:"N/A"
        "${name}: $value"
    }
    return append("$prefix$propStr$postfix")
}