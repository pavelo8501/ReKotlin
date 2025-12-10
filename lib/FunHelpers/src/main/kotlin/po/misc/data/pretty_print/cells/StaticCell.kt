package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.strings.stringify
import po.misc.types.isNotNull
import kotlin.text.append


class StaticCell(
    var content: Any? = null
): PrettyCellBase(Options()){

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
    override fun applyOptions(opt: CommonCellOptions?): StaticCell {
        opt?.let {
            cellOptions = PrettyHelper.toOptions(it)
        }
        return this
    }

    fun render(renderOptions: CellOptions? = null): String {
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
    fun render(): String = render(Options(Orientation.Horizontal))
    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toOptions(commonOptions, cellOptions as Options)
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
            append("id", cellOptions.id)
            append("width", cellOptions.width)
            append(::lockContent)
        }
    }

    companion object
}