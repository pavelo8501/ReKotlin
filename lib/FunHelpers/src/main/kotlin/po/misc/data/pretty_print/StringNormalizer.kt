package po.misc.data.pretty_print


interface StringNormalizer{
    fun shouldNormalize(text: String): Boolean
    fun normalize(text: String): String
}

class DynamicNormalizer(
    val condition: (String)-> Boolean,
    val normalizer: (String)-> String
): StringNormalizer {
    var postfix: String = ""
    constructor(
        postfix: String,
        condition: (String)-> Boolean,
        normalizer: (String)-> String
    ):this(condition, normalizer){
        this.postfix = postfix
    }

    override fun shouldNormalize(text: String): Boolean{
        return condition(text)
    }
    override fun normalize(text: String): String{
        return "${normalizer(text)}${postfix}"
    }
}

class TrimNormalizer(
    maxLength: Int,
    replacementText: String
):StringNormalizer{

    private val sizeNormalization = DynamicNormalizer(
        replacementText,
        { it.length >= maxLength },
        { it.substring(0, maxLength) }
    )
    override fun shouldNormalize(text: String): Boolean = sizeNormalization.shouldNormalize(text)
    override fun normalize(text: String): String = sizeNormalization.normalize(text)
}