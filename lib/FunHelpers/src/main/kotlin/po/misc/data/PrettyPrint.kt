package po.misc.data




interface PrettyPrint: TextBuilder {
    val formattedString: String
}

interface PrettyFormatted {

    fun formatted(renderOnly: List<Enum<*>>? = null): String
    fun formatted(vararg renderOnly: Enum<*>): String = formatted(renderOnly.toList())
}



