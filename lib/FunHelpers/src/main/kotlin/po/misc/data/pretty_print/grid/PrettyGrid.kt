package po.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.ListValueLoader
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.section.PrettySection
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.functions.Throwing
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken


sealed class PrettyGridBase<T: Any>(
    override val typeToken :TypeToken<T>,
    var options: RowOptions,
) :PrettySection<T>, TokenFactory
{

    protected  val rowsBacking: MutableList<PrettyRow<T>> = mutableListOf()
    override val rows: List<PrettyRow<T>> get() = rowsBacking
    override val ids: List<Enum<*>> get() = rows.mapNotNull { it.id }

    protected fun checkShouldRender(row: RenderableElement<*, *>, options: RowOptions): Boolean {
        if (options.renderOnlyList.isEmpty()) return true
        return row.ids.any { it in options.renderOnlyList }
    }

    open fun addRow(row: PrettyRow<T>):PrettyGridBase<T>{
        rowsBacking.add(row)
        return this
    }
}

class PrettyGrid<T: Any>(
    typeToken :TypeToken<T>,
    options: CommonRowOptions = RowOptions(),
) : PrettyGridBase<T>(typeToken, PrettyHelper.toRowOptions(options))
{
    internal val renderBlocksBacking: MutableList<RenderableElement<T, *>> = mutableListOf()
    val renderBlocks: List<RenderableElement<T, *>> get() = renderBlocksBacking

    internal val singleLoader: ValueLoader<T, T> = ValueLoader("ReceiverGrid", typeToken, typeToken)

    fun addRenderBlock(newRenderBlock: RenderableElement<T, *>): PrettyGrid<T>{
        renderBlocksBacking.add(newRenderBlock)
        return this
    }

    override fun addRow(row: PrettyRow<T>): PrettyGridBase<T> {
        rowsBacking.add(row)
        renderBlocksBacking.add(row)
        return this
    }

    override fun render(receiver: T, opts: CommonRowOptions?): String {
        val stringBuilder = StringBuilder()
        for (renderBlock in renderBlocks) {
            val useRender = PrettyHelper.toRowOptions(opts, options)
            val shouldRender = checkShouldRender(renderBlock, useRender)
            if (!shouldRender) continue
            when (renderBlock) {
                is PrettyRow<*> -> {
                    stringBuilder.appendLine(renderBlock.renderOnHost(receiver, opts))
                }
                is PrettyValueGrid<T, *> ->{
                    stringBuilder.appendLine(renderBlock.renderOnHost(receiver, opts))
                }
                else ->{
                    val notSupportedText = "${renderBlock::class} not supported any more"
                    notSupportedText.output(Colour.Yellow)
                }
            }
        }
        return stringBuilder.toString()
    }
}

class PrettyValueGrid<T: Any, V: Any>(
    val hostTypeToken :TypeToken<T>,
    valueToken: TypeToken<V>,
    options: CommonRowOptions = RowOptions(),
) : PrettyGridBase<V>(valueToken, PrettyHelper.toRowOptions(options)), RenderableElement<T, V>
{

    internal val singleLoader: ValueLoader<T, V> = ValueLoader("ReceiverGrid", hostTypeToken, typeToken)

    internal val listLoader = ListValueLoader("ReceiverGrid", hostTypeToken, typeToken)

    override fun renderOnHost(host: T, opts: CommonRowOptions?): String =
        render(host, opts)

    override fun render(receiver: V, opts: CommonRowOptions?): String {
        val stringBuilder = StringBuilder()
        for (row in rows) {
            val useRender = PrettyHelper.toRowOptions(opts, options)
            val shouldRender = checkShouldRender(row, useRender)
            if (!shouldRender) continue
            val render = row.render(receiver, opts)
            stringBuilder.appendLine(render)
        }
        return stringBuilder.toString()
    }

    fun render(receiver: T, opts: CommonRowOptions? = null, valuesAction: ((V)-> Unit)? = null): String {
        if(singleLoader.canLoadValue){
            val value = singleLoader.resolveValue(receiver, Throwing)
            valuesAction?.invoke(value)
            return  render(value, opts)
        }
        if(listLoader.canLoadValue){
            val stringBuilder = StringBuilder()
            val values = listLoader.resolveValue(receiver, Throwing)
            for (value in values){
                valuesAction?.invoke(value)
                val render = render(value, opts)
                stringBuilder.appendLine(render)
            }
            return stringBuilder.toString()
        }
        return SpecialChars.EMPTY
    }
}









