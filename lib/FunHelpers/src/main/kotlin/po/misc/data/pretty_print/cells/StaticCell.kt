package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.strings.stringify
import po.misc.types.isNotNull


class StaticCell(
    var content: Any? = null,
    width: Int = content?.toString()?.length?:0
): PrettyCellBase<PrettyPresets>(width), CellRenderer{

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

    fun render(renderOptions: CommonRenderOptions): String {
        val entry = content.stringify()
        val usedText = if(renderOptions.usePlain){
            entry.text
        } else {
            entry.formatedText
        }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }
    fun render(): String = render(CellRender(Orientation.Horizontal))

    override fun render(content: String, renderOptions: CommonRenderOptions): String {
        val entry = content.stringify()
        val usedText = if(renderOptions.usePlain){
            entry.text
        } else {
            entry.formatedText
        }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }

    override fun toString(): String = "StaticCell [Width: ${options.width}]"

    companion object
}