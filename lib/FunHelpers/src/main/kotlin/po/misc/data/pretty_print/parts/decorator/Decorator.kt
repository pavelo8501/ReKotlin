package po.misc.data.pretty_print.parts.decorator

import po.misc.collections.asList
import po.misc.data.Normalizable
import po.misc.data.Separator
import po.misc.data.Styled
import po.misc.data.helpers.coerceAtLeast
import po.misc.data.logging.Topic
import po.misc.data.logging.Verbosity
import po.misc.data.output.outputBlock
import po.misc.data.pretty_print.parts.common.BorderContainer
import po.misc.data.pretty_print.parts.common.BorderSet
import po.misc.data.pretty_print.parts.common.SeparatorKind
import po.misc.data.pretty_print.parts.common.TaggedSeparator
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.render.BaseRenderParameters
import po.misc.data.pretty_print.parts.render.CanvasLayer
import po.misc.data.pretty_print.parts.render.LayerType
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderRole
import po.misc.data.strings.appendStyled
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.*
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.InstanceMeta
import po.misc.interfaces.named.Named
import po.misc.interfaces.named.NamedComponent
import po.misc.interfaces.named.asNamed
import po.misc.interfaces.named.asNamedComponent

class Decorator(
    val host: NamedComponent
): NamedComponent, TextStyler{

    constructor(hostName: String, vararg separators:TaggedSeparator<BorderPosition>): this(hostName.asNamed()) {
        separators.forEach {
            addSeparator(it)
        }
    }

    data class Snapshot(
        private val classMeta: InstanceMeta,
        internal val metrics: Metrics,
        val policy: DecorationPolicy,
    ): Styled {
        var status: DecorationStatus = DecorationStatus.Undecorated
            internal set

        var bordersSnapshot:List<DecoratorBorder.Snapshot> = emptyList()
            private set

        val maxWidth: Int = metrics.maxWidth
        val leftOffset: Int = metrics.leftOffset

        override val textSpan: TextSpan = buildTextSpan {
            appendLineStyling("Decorating", classMeta.instanceName, ::status, ::policy)
            appendLineStyling("Metrics", metrics)
        }

        internal fun borderSnapshot(snapshot:List<DecoratorBorder.Snapshot>){
            bordersSnapshot = snapshot
        }
    }

    class Metrics(
        override val maxWidth: Int,
        override val leftOffset: Int = 0,
        override val orientation: Orientation = Orientation.Horizontal,
        val linesCount: Int = 0,
    ): BaseRenderParameters{

        constructor(
            parameters:BaseRenderParameters,
            linesCount: Int
        ):this(
            parameters.maxWidth,
            parameters.leftOffset,
            parameters.orientation,
            linesCount
        )
        constructor(content: TextSpan):this(content.plainLength, linesCount = 1)

        val metaText: String get() {
           return buildString {
                appendStyled("Metrics", ::linesCount, ::orientation, ::maxWidth, ::leftOffset)
            }
        }
        override fun toString(): String = metaText
    }

    override val name :String get() = "Decorator"
    override val onBehalfName: TextSpan get() = host.styledName
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

    private val verticalBorderLen: Int get() = leftBorder.displaySize + rightBorder.displaySize

    init { initialBorderConfig() }

    private fun getBorder(position: BorderPosition):DecoratorBorder{
       return when(position){
            BorderPosition.Top -> topBorder
            BorderPosition.Bottom -> bottomBorder
            BorderPosition.Left -> leftBorder
            BorderPosition.Right -> rightBorder
        }
    }

    private fun isTopDecoratable(): Boolean{
       return topBorder.kind != SeparatorKind.LineBreak
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
            if(border.enabled) notify("${border.name} was overwritten", Topic.Debug)
            if(!tagged.enabled){
                notify("Disabled separator supplied for ${border.name}", Topic.Debug)
                return
            }
            border.initBy(tagged)
        }
    }

    private fun createSnapshot(source: Any, metrics: Metrics):Snapshot{
        return Snapshot(ClassResolver.instanceMeta(source), metrics, policy)
    }

    private fun prepareUndecoratedResult(spans: List<TextSpan>, snapshot: Snapshot):DecorationContent{
        val layer =  CanvasLayer(LayerType.Decoration, spans)
        return DecorationContent(name, layer, snapshot)
    }
    private fun prepareResult(spans: List<TextSpan>, snapshot: Snapshot):DecorationContent{
        val layer =  CanvasLayer(LayerType.Decoration, spans)
        snapshot.status = DecorationStatus.Undecorated
        return DecorationContent(name, layer, snapshot)
    }

    private fun horizontalLen(parameters: Metrics): Int{
        val totalWidth = parameters.maxWidth
        return totalWidth + verticalBorderLen
    }
    private fun rightIndentSize(contentWidth: Int, parameters: Metrics): Int{
        val totalWidth = parameters.maxWidth +  parameters.leftOffset
        if(totalWidth >= contentWidth){
            return totalWidth - contentWidth - verticalBorderLen
        }
        return  parameters.leftOffset
    }
    private fun leftIndentSize(parameters: Metrics): Int{
        return  parameters.leftOffset
    }

    private fun createMetrics(content: TextSpan,  linesCount: Int):Metrics{
        return when(content){
            is CanvasLayer ->
                Metrics(content.lineMaxLen, 0, content.displayOrientation, linesCount)
            else -> Metrics(content)
        }
    }
    private fun createMetrics(styled: Styled):Metrics{
        return when(styled){
            is RenderCanvas -> Metrics(styled.lineMaxLen, linesCount =  styled.layersCount)
            else -> Metrics(styled.textSpan)
        }
    }
    private fun createMetrics(renderParams: BaseRenderParameters, linesCount: Int):Metrics{
        return Metrics(renderParams, linesCount)
    }

    private fun renderTop(metrics: Metrics): MutableSpan{
        val span = topBorder.buildRender {
            val totalXLen = horizontalLen(metrics)
            OFFSET_MARGIN.staticMargin(metrics.leftOffset)
            repeatRender(totalXLen)
            render()
        }
        return span
    }
    private fun renderBottom(metrics: Metrics): MutableSpan{
        val span = bottomBorder.buildRender {
            val totalXLen = horizontalLen(metrics)
            OFFSET_MARGIN.staticMargin(metrics.leftOffset)
            repeatRender(totalXLen)
            render()
        }
        return span
    }
    private fun renderLeft(payloads: List<DecorationPayload>, metrics: Metrics){
        payloads.forEach { payload ->
            renderLeft(payload, metrics)
        }
    }
    private fun renderLeft(payload: DecorationPayload, metrics: Metrics){
        val span = leftBorder.buildRender {
            val emptySpaceSize = leftIndentSize(metrics)
            OFFSET_MARGIN.staticMargin(emptySpaceSize)
            render()
        }
        payload.prepend(span)
    }
    private fun renderRight(payloads: List<DecorationPayload>, metrics: Metrics){
        payloads.forEach { payload ->
            renderRight(payload, metrics)
        }
    }
    private fun renderRight(payload: DecorationPayload,  metrics: Metrics){
        val span = rightBorder.buildRender {
            val emptySpaceSize = rightIndentSize(payload.plainLength, metrics)
            OFFSET_MARGIN.staticMargin(emptySpaceSize)
            render()
        }
        payload.append(span)
    }

    private fun decorateContent(
        content: List<MutableSpan>,
        snapshot : Snapshot,
    ):DecorationContent = outputBlock{
        snapshot.output(Topic.Debug)
        if (!enabled){
            prepareUndecoratedResult(content, snapshot)
        }else {
            val processedSpans = mutableListOf<TextSpan>()
            val renderPayloads = content.map { DecorationPayload(LEFT_RIGHT_ORIENTATION, it) }
            if (topBorder.enabled && isTopDecoratable()) {
                val mutable = renderTop(snapshot.metrics).changeRole(RenderRole.TopBorder)
                processedSpans.add(mutable)
            }
            if (rightBorder.enabled) {
                renderRight(renderPayloads, snapshot.metrics)
            }
            if (leftBorder.enabled || snapshot.metrics.leftOffset > 0) {
                renderLeft(renderPayloads, snapshot.metrics)
            }
            val spans = renderPayloads.map { it.commitChanges(RenderRole.Content) }
            processedSpans.addAll(spans)
            if (bottomBorder.enabled) {
                val mutable = renderBottom(snapshot.metrics).changeRole(RenderRole.TopBorder)
                processedSpans.add(mutable)
            }
            val borderSnapshot =  borders.map { it.snapshot() }
            snapshot.borderSnapshot(borderSnapshot)
            val decorationResult = prepareResult(processedSpans, snapshot)
            onBordersApplied?.invoke(decorationResult)
            decorationResult
        }
    }

    fun decorate(record: TextSpan, renderParams: BaseRenderParameters? = null): DecorationContent{

        val content =  when(record){
           is CanvasLayer -> record.text.asMutable().asList()
           is TextSpan -> {
               listOf(CanvasLayer(LayerType.Render, record.asList()))
           }
        }
        val metrics = renderParams?.let {
            createMetrics(it, content.size)
        }?:run {
            createMetrics(record, content.size)
        }
        val snapshot = createSnapshot(record, metrics)
        return decorateContent(content, snapshot)
    }
    fun decorate(records: List<TextSpan>, renderParams: BaseRenderParameters? = null): DecorationContent =
        decorate(CanvasLayer(LayerType.Render, records), renderParams)

    fun decorate(renderParams: BaseRenderParameters, vararg lines: String): DecorationContent =
        decorate(CanvasLayer(LayerType.Render, lines.toMutablePairs()), renderParams)

    fun decorate(vararg lines: String): DecorationContent =
        decorate(CanvasLayer(LayerType.Render, lines.toMutablePairs()), null)

    fun decorate(styled: Styled, renderParams: BaseRenderParameters? = null): DecorationContent {
        if (styled is Normalizable) {
            styled.normalize()
        }
        val content = when (styled) {
            is RenderCanvas -> {
                styled.activeLayers.flatMap {layer->
                    if(layer.layerType == LayerType.Decoration){
                        layer.lines.copyMutablePairs()
                    }else{
                        layer.text.copyMutable().asList()
                    }
                }
            }
            is Styled -> styled.textSpan.copyMutable().asList()
        }
        val metrics = renderParams?.let {
            createMetrics(it, content.size)
        }?:run {
            createMetrics(styled)
        }
        val snapshot = createSnapshot(styled, metrics)
        return decorateContent(content, snapshot)
    }

    fun addSeparator(tagged: TaggedSeparator<BorderPosition>): Decorator{
        initBorder(tagged)
        return this
    }
    fun addSeparator(initializer: BorderContainer){
        initializer.separatorSet.forEach {
            initBorder(it)
        }
    }
    fun addSeparator(borderSet: BorderSet){
        borderSet.separatorSet.forEach {
            addSeparator(it)
        }
    }

    operator fun get(position: BorderPosition): DecoratorBorder = getBorder(position)
    operator fun plus(taggedSeparator: TaggedSeparator<BorderPosition>){
        addSeparator(taggedSeparator)
    }

    companion object {
       private const val OFFSET_MARGIN: String = SpecialChars.WHITESPACE
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
    val config = DecoratorConfigurator(Decorator(asNamedComponent()))
    configAction?.invoke(config)
    return config.decorator
}