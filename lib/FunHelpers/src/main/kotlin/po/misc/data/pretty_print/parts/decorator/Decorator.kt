package po.misc.data.pretty_print.parts.decorator

import po.misc.collections.asList
import po.misc.data.Styled
import po.misc.data.helpers.coerceAtLeast
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.parts.common.BorderInitializer
import po.misc.data.pretty_print.parts.common.SeparatorKind
import po.misc.data.pretty_print.parts.common.TaggedSeparator
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.rendering.BaseRenderParameters
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.*
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceMeta
import po.misc.interfaces.named.Named
import po.misc.interfaces.named.NamedComponent
import po.misc.interfaces.named.asNamed

class Decorator(
    val host: Named
): NamedComponent, TextStyler{

    constructor(
        hostName: String,
        vararg separators:TaggedSeparator<BorderPosition>,
    ): this(hostName.asNamed()) {
        separators.forEach {
            addSeparator(it)
        }
    }

    data class Snapshot(
        val status: DecorationStatus,
        val policy: DecorationPolicy,
        val bordersUsed:List<DecoratorBorder.Snapshot>
    ): Styled {

        override val textSpan: TextSpan = buildTextSpan {
            append(policy)
            append(status)
            append(bordersUsed)
        }
    }

    class Metrics(
        override val contentWidth: Int,
        override val maxWidth: Int = contentWidth,
        override val leftOffset: Int = 0,
    ): BaseRenderParameters

    private val hostName: InstanceMeta get()  {
       return ClassResolver.instanceMeta(host)
    }
    override val name :String get() = "Decorator"
    override val onBehalfName: TextSpan get() = hostName.instanceName

    override var verbosity: Verbosity = Verbosity.Warnings

    var policy: DecorationPolicy = DecorationPolicy.Default
       set(value) {
            field = value
            initialBorderConfig()
       }

    var onBordersApplied: ((DecorationContent) -> Unit)? = null

    val topBorder: DecoratorBorder = DecoratorBorder(BorderPosition.Top, "")
    val bottomBorder: DecoratorBorder = DecoratorBorder(BorderPosition.Bottom, "")
    val leftBorder: DecoratorBorder = DecoratorBorder(BorderPosition.Left, "")
    val rightBorder: DecoratorBorder = DecoratorBorder(BorderPosition.Right, "")

    private val borders = listOf(topBorder, bottomBorder, leftBorder, rightBorder)
    val enabled: Boolean get() = borders.any { it.enabled }

    private val verticalBorderLen : Int  get() = leftBorder.displaySize + rightBorder.displaySize

    init {
        initialBorderConfig()
    }

    private fun getBorder(position: BorderPosition):DecoratorBorder{
       return when(position){
            BorderPosition.Top -> topBorder
            BorderPosition.Bottom -> bottomBorder
            BorderPosition.Left -> leftBorder
            BorderPosition.Right -> rightBorder
        }
    }

    private fun isTopDecoratable(payloads: List<DecorationPayload>): Boolean{
        return !(payloads.isEmpty() && topBorder.kind == SeparatorKind.LineBreak)
    }
    private fun initialBorderConfig(){
        borders.forEach {border->
            if (policy == DecorationPolicy.MandatoryGap && border.tag == BorderPosition.Top) {
                border.repeat = border.repeat.coerceAtLeast(1)
                if (border.text.isEmpty()) {
                    border.text = SpecialChars.NEW_LINE
                }
            }
        }
    }
    private fun initBorder(tagged:TaggedSeparator<BorderPosition>){
        val border = getBorder(tagged.tag)
        if(policy ==  DecorationPolicy.MandatoryGap && tagged.tag == BorderPosition.Top) {
            tagged.repeat = tagged.repeat.coerceAtLeast(1)
            if(tagged.text.isEmpty()){
                tagged.text = SpecialChars.NEW_LINE
            }
            border.initBy(tagged)
        }else{
            if(border.enabled) notify("${border.name} was overwritten", NotificationTopic.Debug)
            if(!tagged.enabled){
                notify("Disabled separator supplied for ${border.name}", NotificationTopic.Debug)
                return
            }
            border.initBy(tagged)
        }
    }
    private fun prepareUndecoratedResult(spans: List<TextSpan>):DecorationContent{
        val actualWidth = spans.maxOfOrNull { it.plainLength }?:0
        val completeRender = spans.joinSpans(Orientation.Vertical)
        return DecorationContent(name, actualWidth, completeRender, spans, createSnapshot(DecorationStatus.Undecorated))
    }
    private fun prepareResult(spans: List<TextSpan>):DecorationContent{
        val actualWidth = spans.maxOfOrNull { it.plainLength }?:0
        val completeRender = spans.joinSpans(Orientation.Vertical)
        return DecorationContent(name, actualWidth, completeRender, spans, createSnapshot(DecorationStatus.Decorated))
    }

    private fun horizontalLen(parameters: BaseRenderParameters): Int{
        val totalWidth = parameters.maxWidth +  parameters.leftOffset
        if(totalWidth > parameters.contentWidth){
            return totalWidth + parameters.leftOffset
        }
        return  parameters.contentWidth + verticalBorderLen
    }
    private fun rightIndentSize(parameters: BaseRenderParameters, contentSize: Int): Int{
        val totalWidth = parameters.maxWidth +  parameters.leftOffset
        if(totalWidth> parameters.contentWidth){
            return totalWidth - contentSize - verticalBorderLen
        }
        return  parameters.leftOffset
    }
    private fun leftIndentSize(parameters: BaseRenderParameters): Int{
        return  parameters.leftOffset
    }

    private fun renderTop(parameters: BaseRenderParameters): TextSpan{
        val span = topBorder.buildRender {
            val totalXLen = horizontalLen(parameters)
            repeatRender(totalXLen)
            render()
        }
        return span
    }
    private fun renderBottom(parameters: BaseRenderParameters, addLineBreak: Boolean = false):TextSpan{
        val span = bottomBorder.buildRender {
            val totalXLen = horizontalLen(parameters)
            repeatRender(totalXLen)
            if(addLineBreak){
                SpecialChars.NEW_LINE.staticMargin()
            }
            render(staticFirst = false)
        }
        return span
    }
    private fun renderLeft(parameters: BaseRenderParameters, payloads: List<DecorationPayload>){
        payloads.forEach { payload ->
            val span = leftBorder.buildRender {
                val emptySpaceSize = leftIndentSize(parameters)
                MARGIN.staticMargin(emptySpaceSize)
                render(staticFirst = false)
            }
            payload.prepend(span)
        }
    }
    private fun renderRight(parameters: BaseRenderParameters, payloads: List<DecorationPayload>){
        payloads.forEach {payload->
            val span = rightBorder.buildRender {
                val emptySpaceSize = rightIndentSize(parameters, payload.plainLength)
                MARGIN.staticMargin(emptySpaceSize)
                render()
            }
            payload.append(span)
        }
    }

    private fun decorateContent(contentLines: List<MutablePair>, parameters: BaseRenderParameters):DecorationContent{
        val processedSpans = mutableListOf<TextSpan>()
        val renderPayloads = contentLines.map { DecorationPayload(LEFT_RIGHT_ORIENTATION, it) }

        if(topBorder.enabled && isTopDecoratable(renderPayloads)){
            val span = renderTop(parameters)
            processedSpans.add(span)
        }
        if(rightBorder.enabled){
           renderRight(parameters, renderPayloads)
        }
        if(leftBorder.enabled){
            renderLeft(parameters, renderPayloads)
        }
        renderPayloads.forEach{payload->
            processedSpans.add(payload.commitChanges())
        }
        if(bottomBorder.enabled){
            processedSpans.add(renderBottom(parameters, false))
        }
        val decorationResult = prepareResult(processedSpans)
        onBordersApplied?.invoke(decorationResult)
        return decorationResult
    }

    fun decorate(records: List<TextSpan>, renderParams: BaseRenderParameters? = null): DecorationContent {
        if (!enabled) {
            return prepareUndecoratedResult(records)
        }
        val params = renderParams?:run {
            val maxLength = records.maxOfOrNull { it.plainLength }?:0
            Metrics(maxLength)
        }
        val contentLines = records.toMutablePairs()
        return decorateContent(contentLines, params)
    }
    @JvmName("decorateSingle")
    fun decorate(record: TextSpan, renderParams: BaseRenderParameters? = null): DecorationContent = decorate(record.asList(), renderParams)

    fun decorate(renderParams: BaseRenderParameters, vararg lines: String): DecorationContent = decorate(lines.toMutablePairs(), renderParams)
    fun decorate(vararg lines: String): DecorationContent = decorate(lines.toMutablePairs(), null)

    fun addSeparator(tagged: TaggedSeparator<BorderPosition>): Decorator{
        initBorder(tagged)
        return this
    }
    fun addSeparator(initializer: BorderInitializer){
        initializer.separatorSet.forEach {
            initBorder(it)
        }
    }

    fun createSnapshot(status: DecorationStatus):Snapshot{
        val enabled = borders.filter { it.enabled }
        val borderSnapshot = enabled.map { it.snapshot() }
        return Snapshot(status, policy, borderSnapshot)
    }
    
    operator fun get(position: BorderPosition): DecoratorBorder = getBorder(position)
    operator fun plus(taggedSeparator: TaggedSeparator<BorderPosition>){
        addSeparator(taggedSeparator)
    }

    companion object{
       private const val MARGIN: String = SpecialChars.WHITESPACE

       val LEFT_RIGHT_ORIENTATION: Orientation = Orientation.Vertical
       val TOP_BOTTOM_ORIENTATION: Orientation = Orientation.Horizontal

       operator fun invoke(
            name:String,
            configAction: DecoratorConfigurator.() -> Unit
        ): Decorator{
            val config = DecoratorConfigurator(Decorator(name))
            configAction.invoke(config)
            return config.decorator
        }
    }
}

fun Named.Decorator(
    configAction: (DecoratorConfigurator.() -> Unit)? = null
): Decorator{
    val config = DecoratorConfigurator(Decorator(this))
    configAction?.invoke(config)
    return config.decorator
}