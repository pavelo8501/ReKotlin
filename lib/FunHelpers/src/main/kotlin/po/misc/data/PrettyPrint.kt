package po.misc.data

import po.misc.data.pretty_print.section.PrettySection
import kotlin.reflect.KProperty0


interface PrettyPrint: TextBuilder {
    val formattedString: String
}

interface PrettyFormatted {

    fun formatted(sections: Collection<Enum<*>>? = null): String
    fun formatted(vararg sections: Enum<*>): String = formatted(sections.toList())
}


fun StringBuilder.appendGroup(prefix: String, postfix: String, vararg props:  KProperty0<*>):StringBuilder{
    val propStr = props.toList().joinToString { "${it.name}: ${it.get().toString()}" }
    return append("$prefix$propStr$prefix")
}



