package po.misc.data.strings

import po.misc.data.helpers.orDefault
import po.misc.debugging.ClassResolver
import po.misc.reflection.displayName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun StringBuilder.className(kClass: KClass<*>): StringBuilder{
    val className = ClassResolver.classInfo(kClass).simpleName
    return appendLine(className)
}


fun StringBuilder.appendLine(vararg strings: String): StringBuilder{
    val list = strings.toList()
    return if(list.isEmpty()){
        this.appendLine(list.joinToString())
    }else{
        this
    }
}

fun StringBuilder.append(vararg strings: String): StringBuilder{
    val list = strings.toList()
    return if(list.isEmpty()){
        this.append(list.joinToString())
    }else{
        this
    }
}

fun StringBuilder.appendParam(parameterName: String, value: Any?): StringBuilder{
    return if(value != null){
        append("$parameterName: $value")
    }else{
        this
    }
}

fun StringBuilder.appendLineParam(parameterName: String, value: Any?): StringBuilder{
    return if(value != null){
        appendLine("$parameterName: $value")
    }else{
        this
    }
}

fun StringBuilder.append(property: KProperty0<*>): StringBuilder{
    val valueStr =  property.get().toString()
    return append("${property.displayName}: $valueStr")
}

fun StringBuilder.appendLine(property: KProperty0<*>): StringBuilder{
    val valueStr =  property.get().toString()
    return appendLine("${property.displayName}: $valueStr")
}

fun StringBuilder.appendGroup(prefix: String, postfix: String, vararg props: KProperty0<*>?):StringBuilder{

    val propStr = props.toList().joinToString {
        val name = it?.name?:"N/A"
        val value = it?.get().toString()?:"N/A"
        "${name}: $value"
    }
    return append("$prefix$propStr$postfix")
}