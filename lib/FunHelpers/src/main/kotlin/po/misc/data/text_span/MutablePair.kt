package po.misc.data.text_span

import po.misc.data.TextWrapper
import po.misc.data.styles.TextStyler

/**
 * Mutable text container used during assembly phases.
 *
 * Unlike [TextSpan], operations mutate internal state and do not
 * produce new instances. Intended for builders and assemblers only.
 */
interface MutableSpan: TextSpan{

    fun prepend(other: TextSpan)
    fun append(other: TextSpan)
    fun change(other: TextSpan):MutableSpan
    fun change(plainText: String, styledText:String = plainText):MutableSpan
    fun changeRole(newRole: SpanRole?):MutableSpan
}


abstract class MutableSpanBase(
    override var role: SpanRole? = null
):MutableSpan{

    fun prepend(plainText: String, styledText:String = plainText){
        prepend(StyledPair(TextStyler.ansi.stripAnsi(plainText), styledText))
    }
    fun append(plainText: String, styledText:String = plainText){
        append(StyledPair(TextStyler.ansi.stripAnsi(plainText), styledText))
    }
    override fun change(plainText: String, styledText:String ):MutableSpanBase{
        change(StyledPair(TextStyler.ansi.stripAnsi(plainText), styledText))
        return this
    }

    override fun changeRole(newRole: SpanRole?):MutableSpanBase{
        if(newRole != null){
            role = newRole
        }
        return this
    }
}

class MutablePair(
    initialPlain: String = "",
    initialStyled: String = initialPlain,
    role: SpanRole? = null
):MutableSpanBase(role){

    constructor(textSpan: TextSpan, newRole: SpanRole? = null):this(textSpan.plain, textSpan.styled, newRole?:textSpan.role)

    override var plain: String = initialPlain
    override var styled: String = initialStyled

    override fun append(other: TextSpan){
        plain += other.plain
        styled += other.styled
        if(role == null){
            role = other.role
        }
    }
    override fun prepend(other: TextSpan){
        plain = "${other.plain}${plain}"
        styled = "${other.styled}${styled}"
        if(role == null){
            role = other.role
        }
    }
    override fun change(other: TextSpan):MutablePair{
        plain = other.plain
        styled = other.styled
        role = other.role
        return this
    }
    override fun change(plainText: String, styledText:String): MutablePair{
        return super.change(plainText, styledText) as MutablePair
    }

    fun toSpan(newRole: SpanRole? = null): StyledPair{
        return StyledPair(plain, styled, newRole?:role)
    }
    override fun copy(newRole: SpanRole?): MutablePair{
        return MutablePair(plain, styled, newRole?:role)
    }
    override fun toString(): String = plain
}






