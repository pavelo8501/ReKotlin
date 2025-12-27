package po.misc.data

import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.strings.StringFormatter
import po.misc.types.k_class.readAllProperties


interface PrettyPrint: TextBuilder {
    val formattedString: String

    companion object:  StringFormatter {

    }
}

interface PrettyFormatted {
    fun formatted(renderOnly: List<RowID>? = null): String
    fun formatted(vararg renderOnly: RowID): String = formatted(renderOnly.toList())
}

inline  fun <reified T:PrettyPrint> T.snapshot():List<String>{
    val snap =  this@snapshot as Any
    val res =  snap::class.readAllProperties(snap)
    return res
}



