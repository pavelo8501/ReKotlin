package po.misc.data.pretty_print.cells

import po.misc.data.isNotNull
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.strings.appendLineParam
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.data.styles.TextStyler
import kotlin.text.append


class StaticCell(
    var sourceContent: Any? = null,
    options:Options = defaultOptions
): PrettyCellBase(options), StaticRender, TextStyler {

    private val sourceContentAsString: String? get() = sourceContent?.toString()

    private var assignedContent: String = "null"
    var text: String
        get() {
            return sourceContent?.toString() ?: assignedContent
        }
        set(value) {
            assignedContent = value
        }
    private val hasContent: Boolean get() = sourceContent != null

    var lockContent: Boolean = false
        internal set

    init {
        if (sourceContent.isNotNull()) {
            lockContent = true
        }
    }
    fun buildText(builderAction: StringBuilder.() -> Unit): StaticCell {
        val result = buildString(builderAction)
        sourceContent = result
        return this
    }

    override fun applyOptions(commonOpt: CommonCellOptions?): StaticCell {
        val options = PrettyHelper.toOptionsOrNull(commonOpt)
        if (options != null) {
            cellOptions = options
        }
        return this
    }
    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toOptions(commonOptions, cellOptions as Options)
        val entry = content.stringify()
        val usedText = if (plainText) {
            entry.plain
        } else {
            entry.formatted
        }
        val formatted = textFormatter.style(usedText)
        val final = justifyText(formatted)
        return final
    }
    override fun render(commonOptions: CommonCellOptions?): String {
        val useOptions = PrettyHelper.toOptions(commonOptions)
        val entry =  sourceContent.stringify()
        val usedText = if (plainText) {
            entry.plain
        } else {
            entry.formatted
        }
        val formatted = textFormatter.style(usedText)
        val final = justifyText(formatted)
        return final
    }
    override fun render(content: Any, commonOptions: CommonCellOptions?): String {
        applyOptions(PrettyHelper.toOptionsOrNull(commonOptions))
        if (hasContent) { return text }
        if (plainText) { return content.toString() }
        if (cellOptions.useSourceFormatting) { return  justifyText(content.stringify().formatted ) }
        val styled = textFormatter.style(text)
        return justifyText(styled)
    }

    override fun copy(): StaticCell{
       return StaticCell(sourceContent, cellOptions.copy())
    }
    override fun toString(): String {
        return buildString {
            appendLine("StaticCell")
            appendLineParam("Width", cellOptions.width)
            appendLineParam(::lockContent)
        }
    }

    companion object {
        val defaultOptions: Options = Options(CellPresets.PlainText)
    }
}