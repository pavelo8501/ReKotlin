package po.misc.data

import po.misc.data.styles.StyleCode


interface Named {
    val name: String
}

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

interface NameValue: Named, TextContaining {
    val value: Int
    override val name: String
    val pairStr: String get() = "${name}: $value"
    override fun asText(): String = "${name}#${value}"
}

interface KeyedValue: Named, TextContaining {
    override val name: String
    val value: String
    val pairStr: String get() = "${name}: $value"
    override fun asText(): String = "${name}#${value}"
}

fun CharSequence.contains(named: Named, ignoreCase: Boolean = false): Boolean =
    indexOf(named.name, ignoreCase = ignoreCase) >= 0

