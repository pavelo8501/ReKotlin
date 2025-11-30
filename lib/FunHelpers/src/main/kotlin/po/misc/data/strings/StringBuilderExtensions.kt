package po.misc.data.strings

import po.misc.data.helpers.orDefault
import po.misc.debugging.ClassResolver
import po.misc.reflection.displayName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

fun StringBuilder.className(kClass: KClass<*>): StringBuilder{
    val className = ClassResolver.classInfo(kClass).simpleName
    return appendLine(className)
}

fun StringBuilder.classParam(parameterName: String, value: Any?): StringBuilder{
    val valueStr = value.orDefault("N/A")
    return appendLine("$parameterName: $valueStr")
}

fun StringBuilder.classProperty(property: KProperty0<*>): StringBuilder{
    val valueStr =  property.get().toString()
    return appendLine("${property.displayName}: $valueStr")
}