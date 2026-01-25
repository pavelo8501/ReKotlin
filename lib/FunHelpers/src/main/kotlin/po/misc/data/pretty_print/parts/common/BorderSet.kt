package po.misc.data.pretty_print.parts.common

import po.misc.data.output.output
import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.styles.Colour
import po.misc.data.styles.StyleCode
import po.misc.exceptions.checkIfTrue


interface BorderSet{
    val separatorSet: List<TaggedSeparator<BorderPosition>>
    val hasEnabledSeparators: Boolean get() {
        return separatorSet.any { it.enabled }
    }
}

interface BorderInitializer: BorderSet{

    private fun matchExistent(position: BorderPosition):TaggedSeparator<BorderPosition>?{
        return separatorSet.firstOrNull { it.tag== position }
    }

    fun topBorder(separatorText: String, styleCode: StyleCode? = null) {
        separatorSet.firstOrNull { it.tag == BorderPosition.Top }?.initBy(separatorText, styleCode)
    }
    fun bottomBorder( separatorText: String, styleCode: StyleCode? = null) {
        separatorSet.firstOrNull { it.tag == BorderPosition.Bottom }?.initBy(separatorText, styleCode)
    }
    fun leftBorder( separatorText: String, styleCode: StyleCode? = null){
        separatorSet.firstOrNull { it.tag == BorderPosition.Left }?.initBy(separatorText, styleCode)
    }
    fun rightBorder(separatorText: String, styleCode: StyleCode? = null) {
        separatorSet.firstOrNull { it.tag == BorderPosition.Right }?.initBy(separatorText, styleCode)
    }

    fun border(vararg separator: TaggedSeparator<BorderPosition>){
        separator.forEach {separator->
           val found = matchExistent(separator.tag)
           if(found != null){
               found.initBy(separator)
           }else{
               "SeparatorSet does not contain matching $separator separator".output(Colour.YellowBright)
           }
        }
    }

    fun borders(borderPresets: BorderPresets, styleCode: StyleCode? = null){
        BorderPosition.entries.forEach { entry->
            borderPresets.separatorSet.firstOrNull { it.tag == entry }?.let {presetSeparator->
                separatorSet.firstOrNull { it.tag ==  presetSeparator.tag }?.let {foundTagged->
                    foundTagged.setStyle(styleCode)
                    foundTagged.initBy(presetSeparator)
                    foundTagged.checkIfTrue(foundTagged.text.isNotEmpty()){
                        "After copy $it has empty text parameter".output(Colour.YellowBright)
                    }
                }?:run {
                    "Nothing found for $entry".output(Colour.YellowBright)
                }
            }
        }
    }
}