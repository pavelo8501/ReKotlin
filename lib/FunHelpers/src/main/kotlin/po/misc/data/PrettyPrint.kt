package po.misc.data

import po.misc.data.pretty_print.parts.RowID


interface PrettyPrint: TextBuilder {
    val formattedString: String
}

interface PrettyFormatted {

    fun formatted(renderOnly: List<RowID>? = null): String
    fun formatted(vararg renderOnly: RowID): String = formatted(renderOnly.toList())
}



