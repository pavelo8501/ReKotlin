package po.misc.data

interface HasValue: TextContaining {
    val value: String

    override fun asText(): String = value
}

interface TextContaining {
    fun asText(): String
}

