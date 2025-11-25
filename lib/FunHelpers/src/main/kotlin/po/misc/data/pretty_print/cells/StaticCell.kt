package po.misc.data.pretty_print.cells

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.strings.stringify


class StaticCell(
    var content: Any? = "",
    width: Int = content.toString().length + 2
): PrettyCellBase<PrettyPresets>(width, Align.LEFT), CellRenderer, PrettyPrint {

    val text: String get() = content.stringify().toString()

    override var preset: PrettyPresets? = null
    override val formattedString: String get() = text

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

    fun render(renderOptions : RenderOptions =  RenderOptions()): String {
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


    companion object
}