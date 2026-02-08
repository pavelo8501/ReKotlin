package po.misc.data

import po.misc.data.styles.SpecialChars
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan


sealed interface TextModifier

sealed interface TextSeparator : TextModifier{
    fun wrap(text1: String,text2: String): String
}

sealed interface TextWrapper: TextModifier{
    fun wrap(text: String): String
    fun wrap(span: TextSpan): TextSpan
    fun wrapMutating(mutable: MutableSpan)

    fun hasText(compareText: String): Boolean{
        return when(this){
            is Prefix ->{ this.value == compareText}
            is StringModifyParams ->{
                this.hasText(compareText)
            }
            else -> this.toString() == compareText
        }
    }

    fun hasBreakLine(): Boolean = hasText(SpecialChars.NEW_LINE)
    fun hasPostfix(compareText: String): Boolean{
       return postfixOrEmpty() == compareText
    }
    fun postfixOrEmpty():String{
        if(this is Postfix){
            return value
        }
        if(this is StringModifyParams){
            return  this.postfix.value
        }
        return ""
    }
}

@JvmInline
value class Separator(val value: String = SpecialChars.WHITESPACE): TextSeparator{
    override fun wrap(text1: String, text2: String): String{
        return text1 + value + text2
    }
}

@JvmInline
value class Prefix(val value: String = SpecialChars.EMPTY): TextWrapper{
    override fun wrap(text: String): String{
        return value + text
    }
    override fun wrap(span: TextSpan): TextSpan{
        if(value.isEmpty()){
            return span
        }
        val plainText = value + span.plain
        val styledText = value + span.styled
        return when(span){
            is MutableSpan -> MutablePair(plainText, styledText)
            is TextSpan -> StyledPair(plainText, styledText)
        }
    }

    override fun wrapMutating(mutable: MutableSpan){
        val plainText = value + mutable.plain
        val styledText = value + mutable.styled
        mutable.change(plainText, styledText)
    }
}

@JvmInline
value class Postfix(val value: String = SpecialChars.EMPTY) :TextWrapper{
    override fun wrap(text: String): String{
        return  text + value
    }
    override fun wrap(span: TextSpan): TextSpan{
        if(value.isEmpty()){
            return span
        }
        val plainText = span.plain + value
        val styledText = span.styled + value
        return when(span){
            is MutableSpan -> MutablePair(plainText, styledText)
            is TextSpan -> StyledPair(plainText, styledText)
        }
    }
    override fun wrapMutating(mutable: MutableSpan){
        val plainText = mutable.plain + value
        val styledText = mutable.styled + value
        mutable.change(plainText, styledText)
    }
}

data class StringModifyParams(
    var separator: Separator,
    var prefix: Prefix = Prefix(),
    var postfix: Postfix = Postfix(),
):TextWrapper, TextSeparator, TextModifier{

    constructor(prefix: Prefix, postfix : Postfix):this(Separator(SpecialChars.EMPTY), prefix, postfix)

    fun initialize(modifier: TextWrapper, separator: Separator? = null){
        when(modifier){
            is Prefix -> prefix = modifier
            is Postfix -> postfix = modifier
            is StringModifyParams -> {
               this.separator = modifier.separator
                prefix = modifier.prefix
                postfix = modifier.postfix
            }
        }
        if(separator != null){
            this.separator = separator
        }
    }

    override fun wrap(text1: String, text2: String): String{
        return text1 + separator.value + text2
    }

    override fun hasText(compareText: String): Boolean{
        if(separator.value == compareText){
            return true
        }
        if(prefix.value == compareText){
            return true
        }
        return postfix.value == compareText
    }
    override fun wrap(text:String): String{
       return postfix.wrap(prefix.wrap(text))
    }

    override fun wrap(span: TextSpan): TextSpan{
        val plainText = postfix.wrap(prefix.wrap(span.plain))
        val styledText = postfix.wrap(prefix.wrap(span.styled))
        return when(span){
            is MutableSpan ->{
                span.change(plainText, styledText)
            }
            is TextSpan -> {
                StyledPair(plainText, styledText)
            }
        }
    }
    override fun wrapMutating(mutable: MutableSpan){
        val plainText = postfix.wrap(prefix.wrap(mutable.plain))
        val styledText = postfix.wrap(prefix.wrap(mutable.styled))
        mutable.change(plainText, styledText)
    }
}

