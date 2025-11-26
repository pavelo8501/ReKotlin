package po.misc.data.pretty_print.formatters

class NormalizeManager(
    vararg val conditions: StringNormalizer
): StringNormalizer {

    internal var normalizers = mutableListOf<StringNormalizer>()

    init {
        normalizers.addAll(conditions.toList())
    }

    fun addNormalizer(normalizer: StringNormalizer): NormalizeManager{
        normalizers.add(normalizer)
        return this
    }

    override fun shouldNormalize(text: String): Boolean {
       return normalizers.map { it.shouldNormalize(text) }.any { it }
    }

    override fun normalize(text: String): String{
        var resultingText = text
        normalizers.forEach {
            if(it.shouldNormalize(resultingText)){
                resultingText = it.normalize(resultingText)
            }
        }
        return resultingText
    }

    fun reset(): Unit = normalizers.clear()

}