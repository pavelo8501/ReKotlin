package po.misc.data.text_span

import po.misc.collections.ElementPosition
import po.misc.collections.forEachButLast
import po.misc.collections.forEachPositioned
import po.misc.collections.onLastIfAny
import po.misc.data.Prefix
import po.misc.data.TextWrapper
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.types.isSubclassOf
import kotlin.reflect.full.isSubclassOf


fun TextSpan.copyMutable(newRole: SpanRole? = null):MutablePair =
    when(val span =  this){
        is MutablePair -> span.copy(newRole)
        is TextSpan -> MutablePair(span, newRole?:span.role)
    }

fun TextSpan.asMutable(newRole: SpanRole? = null):MutablePair =
    when(val span =  this){
        is MutablePair -> span.changeRole(newRole) as MutablePair
        is TextSpan -> MutablePair(span, newRole?:span.role)
    }

infix fun TextSpan.prependCreating(text: String):TextSpan {
    val thisSpan = this
    return StyledPair(TextStyler.ansi.stripAnsi(text) + thisSpan.plain, text + thisSpan.styled, thisSpan.role)
}

infix fun TextSpan.prependCreating(other: TextSpan):TextSpan {
   val thisSpan = this
   return StyledPair(other.plain + thisSpan.plain, other.styled + thisSpan.styled, thisSpan.role)
}

infix fun TextSpan.appendCreating(text: String):TextSpan {
    val thisSpan = this
    return StyledPair(thisSpan.plain + TextStyler.ansi.stripAnsi(text), thisSpan.styled + text, thisSpan.role)
}

infix fun TextSpan.appendCreating(other: TextSpan):TextSpan {
    val thisSpan = this
    return StyledPair(thisSpan.plain + other.plain, thisSpan.styled + other.styled, thisSpan.role)
}


fun List<TextSpan>.joinSpans(wrapper: TextWrapper? = null, role: SpanRole? = null): TextSpan{
    val builder = SpanBuilder()
    forEachPositioned{ position, span ->
        if(wrapper != null){
            when(position) {
                ElementPosition.Single -> builder.append(span)
                ElementPosition.First ->{
                    if(wrapper.hasText(SpecialChars.WHITESPACE)){
                        builder.append(span)
                    }else{
                        builder.append(wrapper.wrap(span))
                    }
                }
                ElementPosition.Middle -> builder.append(wrapper.wrap(span))
                ElementPosition.Last -> builder.append(span)
            }
        }else{
            builder.append(span)
        }
    }
    return builder.toMutableSpan(role)
}

fun List<TextSpan>.joinSpans(orientation: Orientation, role: SpanRole? = null): TextSpan{
   return if(orientation == Orientation.Horizontal){
        joinSpans(Prefix(SpecialChars.WHITESPACE), role)
    }else{
        joinSpans(Prefix(SpecialChars.NEW_LINE), role)
    }
}

inline fun <reified R: TextSpan> List<TextSpan>.joinSpansAs(modifier: TextWrapper? = null, role: SpanRole? = null): R{
    val builtSpan = this.joinSpans(modifier, role)
    val resultClass = R::class
    return when{
        resultClass.isSubclassOf<MutableSpan>()-> builtSpan.asMutable() as R
        resultClass.isSubclassOf<TextSpan>() -> builtSpan.copy() as R
        else -> builtSpan.copy() as R
    }
}

inline fun <reified R: TextSpan> List<TextSpan>.joinSpansAs(orientation: Orientation, role: SpanRole? = null): R{
    return if(orientation == Orientation.Horizontal){
        joinSpansAs<R>(Prefix(SpecialChars.WHITESPACE), role)
    }else{
        joinSpansAs<R>(Prefix(SpecialChars.NEW_LINE), role)
    }
}

inline fun <T: TextSpan> T.whenRole(role: SpanRole, block: T.() -> Unit){
    if(this.role == role){
        block()
    }
}

