package po.misc.data.pretty_print.formatters

interface StringNormalizer{
    fun shouldNormalize(text: String): Boolean
    fun normalize(text: String): String
}