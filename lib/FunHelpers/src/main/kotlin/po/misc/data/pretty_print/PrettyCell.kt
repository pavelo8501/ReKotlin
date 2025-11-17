package po.misc.data.pretty_print

import po.misc.data.styles.BGColour

import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler


/**
 * A single formatted cell used for pretty-printing aligned and styled text.
 *
 * A cell consists of:
 *  - a fixed or clamped width
 *  - optional alignment rule
 *  - optional text style (bold, italic…)
 *  - optional colour and background
 *  - optional postfix appended after formatting
 *  - optional dynamic [StringNormalizer] applied before styling
 *
 * A [PrettyCell] is *lightweight* and stateless; rendering happens per input.
 */
class PrettyCell(
    val width: Int,
    val align: Align = Align.LEFT,
    val style: TextStyle? = null,
    val color: Colour? = null,
    val backGround: BGColour? = null,
    var defaults: RenderDefaults = Console80,
): CellRenderer {

    var cellPostfix: String = ""
    var emptySpaceFiller: Char? = null

    val textNormalizer: NormalizeManager = NormalizeManager()

    constructor(
        width: Int,
        preset: PrettyPresets,
        defaults: RenderDefaults = Console80
    ):this(width,preset.align, preset.style, preset.colour, preset.background, defaults){
        cellPostfix = preset.postfix
    }

    private fun applyWidth(text: String, width: Int): String {
        val filler = emptySpaceFiller
        return when (align) {
            Align.LEFT -> {
                if(filler == null){
                    text.padEnd(width)
                }else{
                    val withWhitespace = "$text "
                    withWhitespace.padEnd(width, filler)
                }
            }
            Align.RIGHT -> {
                if(filler == null){
                    text.padStart(width)
                }else{
                    text.padStart(width, filler)
                }
            }
            Align.CENTER -> {
                val filletString = filler?.toString()?:""
                val diff = width - text.length
                val left = diff / 2
                val right = diff - left
                filletString.repeat(left) + text + filletString.repeat(right)
            }
        }
    }

    private fun applyStyle(text: String): String = TextStyler.style(text, style, color, backGround)

    private fun applyCellFormatting(text: String): String{
        return if (cellPostfix.isNotEmpty()) text + cellPostfix else text
    }

    /**
     * Adds a transformer applied before width, postfix, and styling.
     */
    fun addTextNormalizer(normalizer: StringNormalizer): PrettyCell{
        textNormalizer.addNormalizer(normalizer)
        return this
    }

    /**
     * Apply normalization → postfix → styling → width.
     */
    override fun render(content: String): String {
        var formatted = content
        textNormalizer.let {normalizer->
            formatted = normalizer.normalize(content)
        }
        formatted = applyCellFormatting(formatted)
        val styled = applyStyle(formatted)
        val usedWidth =  width.coerceAtMost(defaults.DEFAULT_WIDTH)
        val final = applyWidth(styled, usedWidth)
        return final
    }

    fun resetNormalizers(){
        textNormalizer.normalizers.clear()
    }

    companion object{
        fun build(width: Int, builder: PrettyCell.()-> Unit ): PrettyCell{
            val cell = PrettyCell(width)
            cell.builder()
            return cell
        }

        fun copyKeyParams(source: PrettyCell, target: PrettyCell):PrettyCell{
            target.cellPostfix = source.cellPostfix
            target.emptySpaceFiller = source.emptySpaceFiller
            return target
        }
    }
}
