package po.misc.data.pretty_print.parts.decorator

import po.misc.collections.ReactiveList
import po.misc.collections.lastIndexedOrNull
import po.misc.collections.reactiveListOf
import po.misc.data.helpers.coerceAtLeast
import po.misc.data.helpers.repeat
import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.pretty_print.parts.common.Separator
import po.misc.data.pretty_print.parts.common.SeparatorBase
import po.misc.data.pretty_print.parts.common.SeparatorKind
import po.misc.data.pretty_print.parts.common.TaggedDecorator
import po.misc.data.pretty_print.parts.common.toSeparator
import po.misc.data.pretty_print.parts.decorator.DecoratorBorder.BorderRender
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.append
import po.misc.data.text_span.joinSpans
import po.misc.data.text_span.prepend


enum class BorderPosition(val orientation: Orientation){
    Top(Orientation.Horizontal),
    Right(Orientation.Vertical),
    Left(Orientation.Vertical),
    Bottom(Orientation.Horizontal),
}

interface RenderBuilder{
    val displaySize: Int
    fun String.staticMargin(repeat: Int = 1)
    fun repeatRender(times: Int)
    fun render(staticFirst: Boolean = true):BorderRender
}

class DecoratorBorder(
    val position: BorderPosition,
    initialText: String,
    initialStyle: StyleCode? = null
):RenderSchemeBuilder, RenderBuilder, TaggedDecorator<BorderPosition> {

    class BorderRender internal constructor(val result: TextSpan)

    data class Snapshot(
        val position: BorderPosition,
        val displaySize: Int,
        val enabled:Boolean,
        val producedContent: String,
        val separatorsUsed: List<SeparatorBase.Snapshot>,
    )

    val renderScheme: ReactiveList<Separator> = reactiveListOf{ }
    override val tag: BorderPosition = position
    override var repeat: Int = 1
    /**
     * Indicates whether this separator produces any visible output.
     */
    override val enabled:Boolean get() = renderScheme.any { it.enabled }

    /**
     * Concatenated plain text representation of the render scheme.
     */
    override var text: String
        get() = renderScheme.joinToString(SpecialChars.EMPTY){ it.text }
        set(value) {
            setScheme(value, repeat)
        }

    override val styleCode: StyleCode? = renderScheme.lastOrNull()?.styleCode
    override val size: Int get() = text.length

    val name: String get() = "DecoratorBorder[$position]"

    val kind: SeparatorKind get() {
        val hasLinBrk = renderScheme.any { it.kind ==  SeparatorKind.LineBreak }
        if (hasLinBrk) {
          return  SeparatorKind.LineBreak
        }
        return SeparatorKind.Fill
    }

    /**
     * Visible display size. Returns zero if the separator is disabled.
     */
    override val displaySize: Int get() {
        if(enabled) return size
        return 0
    }
    val info: String get() = buildString {
        append("Text: '$text' ")
        append("Enabled: $enabled ")
    }
    private var tempMargin: String? = null
    private var tempRepeat: Int? = null
    init {
        if(initialText.isNotEmpty()) {
            renderScheme.add(Separator(initialText, initialStyle))
        }
    }
    private fun completeRender(times: Int?):TextSpan{
       val result = renderScheme.mapNotNull { sep ->
            if(!sep.hasRepeat && times != null){
                 sep.toPair(times)
            }else{
                sep.toPair()
            }
        }
        return result.joinSpans(Orientation.Horizontal)
    }

    private fun renderInDSL(staticFirst: Boolean): TextSpan{
        val repeat = tempRepeat
        val result =  if(staticFirst){
            completeRender(repeat).prepend(tempMargin?:"")
        }else{
            completeRender(repeat).append(tempMargin?:"")
        }
        tempMargin = null
        tempRepeat = null
        return result
    }

    override fun initBy(extendedString: ExtendedString): DecoratorBorder{
        renderScheme.clear()
        when(extendedString){
            is DecoratorBorder ->{
                if(extendedString.renderScheme.isNotEmpty()){
                    renderScheme.addAll(extendedString.renderScheme)
                }else{
                    renderScheme.add(extendedString.toSeparator())
                }
            }
            else -> {
                renderScheme.add(extendedString.toSeparator())
            }
        }
        return this
    }
    override fun setScheme(text:String, repeat: Int, styleCode: StyleCode?):DecoratorBorder{
        renderScheme.clear()
        val separator = Separator(text, styleCode, repeat)
        renderScheme.add(separator)
        return this
    }

    override fun append(separatorText:String, repeat: Int, styleCode: StyleCode? ):DecoratorBorder{
        renderScheme.lastOrNull()?.let {
            val separator = Separator(separatorText, styleCode, repeat)
            renderScheme.add(separator)
        }
        return this
    }
    override fun prepend(separatorText:String, repeat: Int, styleCode: StyleCode? ):DecoratorBorder{
        renderScheme.lastIndexedOrNull{ index, _ ->
            val separator = Separator(separatorText, styleCode, repeat)
            renderScheme.add((index -1).coerceAtLeast(0), separator)
        }
        return this
    }

    fun buildScheme(builderAction: RenderSchemeBuilder.()->Unit){
        builderAction.invoke(this)
    }
    override fun String.staticMargin(repeat: Int){
        if(repeat <=0 ) return
        tempMargin =  this.repeat(repeat)
    }
    override fun buildRender(buildAction: RenderBuilder.()->BorderRender): TextSpan{
        val render = buildAction.invoke(this)
        return render.result
    }
    override fun repeatRender(times: Int){
        if(times <=0 ) return
        tempRepeat = times
    }
    override fun render(staticFirst: Boolean):BorderRender {
        val span = renderInDSL(staticFirst)
        return BorderRender(span)
    }
    fun snapshot():Snapshot{
        return Snapshot(position, displaySize, enabled, text, renderScheme.map { it.snapshot() } )
    }
    fun toString(times: Int):String {
        val pair = StyledPair()
        completeRender(times)
        return pair.styled
    }
    override fun toString(): String {
        val pair = StyledPair()
        completeRender(null)
        return pair.styled
    }

    fun copy(): DecoratorBorder{
        return DecoratorBorder(tag, "").also {copy->
            if(renderScheme.isNotEmpty()){
                val listExceptCreated = renderScheme
                copy.renderScheme.addAll(listExceptCreated)
            }
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

    companion object{

        operator fun invoke(
            direction: BorderPosition,
            builderAction: RenderSchemeBuilder.()->Unit
        ):DecoratorBorder{
            val separator = DecoratorBorder(direction, "")
            builderAction.invoke(separator)
            return separator
        }

        operator fun invoke(
            direction: BorderPosition,
            borderChar: Char,
            styleCode: StyleCode? = null,
            enabled:Boolean = true
        ):DecoratorBorder {
            val sep = Separator(borderChar, styleCode)
            sep.enabled = enabled
            return DecoratorBorder(direction, "").initBy(sep)
        }
    }
}
