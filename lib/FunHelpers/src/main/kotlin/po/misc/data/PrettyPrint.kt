package po.misc.data

import po.misc.data.pretty_print.section.PrettySection


interface PrettyPrint: TextBuilder {
    val formattedString: String
}

interface PrettyFormatted {

    fun formatted(sections: Collection<Enum<*>>? = null): String
    fun formatted(vararg sections: Enum<*>): String = formatted(sections.toList())
}

