package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.classParam
import po.misc.data.strings.classProperty
import po.misc.data.strings.stringify
import po.misc.types.isNotNull


class StaticCell(
    var content: Any? = null,
    cellOptions: CellOptions = CellOptions(),
    row: PrettyRow<*>? = null
): PrettyCellBase<PrettyPresets>(cellOptions, row), CellRenderer{

    val text: String get() = content.stringify().toString()

    var lockContent: Boolean = false
        internal set

    init {
        if(content.isNotNull()){
            lockContent = true
        }
    }

    fun changeContent(newContent: Any):StaticCell{
        content = newContent
        return this
    }
    fun changeText(text: String):StaticCell{
        content = text
        return this
    }
    fun applyText(textProvider: ()-> String ):StaticCell{
        content = textProvider()
        return this
    }
    fun buildText(builderAction: StringBuilder.() -> Unit):StaticCell{
        val result = buildString(builderAction)
        content = result
        return this
    }

    fun render(renderOptions: CommonCellOptions? = null): String {
        val useOptions = renderOptions?:cellOptions
        val entry = content.stringify()
        val usedText = if(useOptions.usePlain){
            entry.text
        } else {
            entry.formatedText
        }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  useOptions)
        return final
    }
    fun render(): String = render(CellOptions(Orientation.Horizontal))

    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toCellOptionsOrDefault(commonOptions, cellOptions as CellOptions)
        val entry = content.stringify()
        val usedText = if(options.usePlain){
            entry.text
        } else {
            entry.formatedText
        }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    override fun toString(): String {
        return buildString {
            appendLine("StaticCell")
            classParam("id", cellOptions.id)
            classParam("width", cellOptions.width)
            classProperty(::lockContent)
        }
    }
    companion object
}