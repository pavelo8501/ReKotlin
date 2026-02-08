package po.misc.data.pretty_print.parts.common

import po.misc.data.helpers.coerceAtLeast
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.SpanRole
import po.misc.data.text_span.StyledPair
import po.misc.types.k_class.simpleOrAnon


enum class SeparatorKind {
    Fill,
    LineBreak
}

interface SeparatorContract{
    val repeat: Int
    val hasRepeat: Boolean get() = repeat > 1
    val kind: SeparatorKind
}

interface TaggedDecorator<E: Enum<E>>: ExtendedString{
    val tag: E
    var repeat: Int
}

abstract class SeparatorBase(
    initialText: String?,
    override var styleCode: StyleCode? = null,
    override var repeat: Int = 1,
): ExtendedString, SeparatorContract{

    data class Snapshot(
        val name: String,
        val kind: SeparatorKind,
        val text: String,
        val repeat: Int,
        val enabled: Boolean,
    )

    final override var enabled:Boolean = false

    override var kind: SeparatorKind = SeparatorKind.Fill
      internal set

    override var text:String = ""
        set(value) {
            field = value
            enabled = value.isNotEmpty()
            if(text == SpecialChars.NEW_LINE){
                kind = SeparatorKind.LineBreak
            }
        }
    init {
        text = initialText?:""

    }
    private fun createPair(times: Int, role: SpanRole? = null):StyledPair?{
        if(!enabled){ return null }

        if(kind == SeparatorKind.LineBreak){
            return StyledPair(text, role = role)
        }
        val string = text.repeat(times)
        return  styleCode?.let {
            StyledPair(string,  string.style(it), role)
        }?:run {
            StyledPair(string,  string, role)
        }
    }
    private fun createPair(role: SpanRole? = null):StyledPair?{
        if(!enabled){ return null }
        return  styleCode?.let {
            StyledPair(text,  text.style(it), role)
        }?:run {
            StyledPair(text, role = role)
        }
    }

    abstract fun copy(): SeparatorBase

    fun initBy(separatorString:String,  styleCode: StyleCode?, repeat: Int = 1){
        text = separatorString
        this.styleCode = styleCode
        this.repeat = repeat.coerceAtLeast(1)
    }
    fun initBy(extendedString: ExtendedString){
        text = extendedString.text
        styleCode = extendedString.styleCode
        enabled = extendedString.enabled
        if(extendedString is Separator){
            repeat = extendedString.repeat.coerceAtLeast(1)
        }
    }

    fun toPair():StyledPair? = createPair()
    fun toPair(times: Int):StyledPair? = createPair(times)

    fun snapshot():Snapshot{
       val separatorName = if(this is TaggedSeparator<*>){
            this.tag.name
        }else{
            this::class.simpleOrAnon
        }
       return Snapshot(separatorName, kind, text, repeat, enabled)
    }

    fun toString(times: Int):String{
        if(times <= 0){
            return ""
        }
        return createPair(times)?.styled?:""
    }
    override fun toString():String{
        return createPair()?.styled?:""
    }
}

/**
 * A concrete, untagged separator implementation.
 *
 * This separator is typically used for structural elements such as borders,
 * dividers, or repeated characters in layout rendering.
 *
 * Equality is based on:
 * - textual content
 * - enabled state
 * - applied style
 *
 * Repeat count is intentionally excluded from equality checks, as repetition
 * is considered a rendering concern rather than identity.
 */

class Separator(
    text: String,
    styleCode: StyleCode? = null,
    repeat: Int = 1,
): SeparatorBase(text, styleCode, repeat){

    constructor(extended: ExtendedString?):this(extended?.text?:"", extended?.styleCode){
        enabled = extended?.enabled?:false
    }

    constructor(char: Char, styleCode: StyleCode? = null, repeat: Int = 1):this(char.toString(), styleCode, repeat)

    override fun copy(): Separator {
        return Separator(text, styleCode, repeat).also {
            it.enabled = enabled
        }
    }

    override fun equals(other: Any?): Boolean {
        when(other) {
            is ExtendedString -> {
                if(text != other.text) return false
                if(enabled != other.enabled) return false
                if(styleCode != other.styleCode) return false
                return true
            }
            else -> return false
        }
    }
    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + (styleCode?.hashCode() ?: 0)
        result = 31 * result + text.hashCode()
        return result
    }
}

/**
 * A separator associated with a semantic tag.
 *
 * Tagged separators are primarily used in layout systems where separator
 * placement is determined by meaning rather than declaration order
 * (e.g. borders identified by direction).
 *
 * The [tag] is part of the separator’s identity and is included in equality
 * comparisons.
 *
 * Tagged separators can safely be supplied in any order and later resolved
 * by tag.
 *
 * @param tag semantic identifier (e.g. border direction)
 */
class TaggedSeparator<E: Enum<E>>(
    override val tag: E,
    text: String? = null,
    styleCode: StyleCode? = null,
    repeat: Int = 1,
): SeparatorBase(text, styleCode, repeat),TaggedDecorator<E>{

   constructor(tag: E, char: Char, styleCode: StyleCode? = null, repeat: Int = 1):this(tag, char.toString(), styleCode, repeat)

    override fun copy(): TaggedSeparator<E> {
        return TaggedSeparator(tag, text, styleCode, repeat).also {
            it.enabled = enabled
        }
    }
    fun setStyle(style: StyleCode?):TaggedSeparator<E>{
        styleCode = style
        return this
    }
    override fun equals(other: Any?): Boolean {
        when(other) {
            is TaggedSeparator<*> -> {
                if(text != other.text) return false
                if(enabled != other.enabled) return false
                if(styleCode != other.styleCode) return false
                if(tag != other.tag) return false
                return true
            }
            else -> return false
        }
    }
    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + (styleCode?.hashCode() ?: 0)
        result = 31 * result + tag.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}