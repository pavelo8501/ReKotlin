package po.misc.data


interface TextContaining {
    fun asText(): String
}

interface HasValue: TextContaining {
    val value: Int
    override fun asText(): String = value.toString()
}

interface HasText: TextContaining {
    val value: String
    override fun asText(): String = value
}

interface HasNameValue: TextContaining {
    val value: Int
    val name: String
    override fun asText(): String = "${name}#${value}"
}


interface HasKeyValuePair{
    val name: String
    val value: String
    val pairStr: String get() = "${name}: $value"
}



