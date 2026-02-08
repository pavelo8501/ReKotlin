package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.pretty_print.formatters.StyleFormatter
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.render.StyleParameters
import po.misc.data.styles.Colour
import po.misc.data.styles.applyColour
import po.misc.data.text_span.EditablePair
import po.misc.data.text_span.MutableSpan


open class ColorModifier(
    vararg val conditions: ColourCondition
): StyleFormatter {

    override val dynamic: Boolean = false
    override val tag : FormatterTag = FormatterTag.ColorModifier
    var provider: (()-> Colour?)? = null

    constructor(colourProvider: ()-> Colour?):this()

    class ColourCondition(
        val match: String,
        val colour: Colour,
        val matchType: Match = Match.Partially
    ){
        enum class Match{Partially, Exactly}
    }

    private val conditionsList: MutableList<ColourCondition> = mutableListOf()

    init {
        conditionsList.addAll(conditions.toList())
    }
    private fun modifyWithProvider(text: String, provider: ()-> Colour?): String{
        val colour = provider.invoke()
        if(colour != null){
            return text.applyColour(colour)
        }
        return text
    }
    private fun modifyByConditions(text: String, conditions : List<ColourCondition>): String{
        for (condition in conditions){
            when(condition.matchType){
                ColourCondition.Match.Partially -> {
                    if(text.contains(condition.match)){
                        return  text.applyColour(condition.colour)
                    }
                }
                ColourCondition.Match.Exactly ->{
                    if(text == condition.match){
                        return  text.applyColour(condition.colour)
                    }
                }
            }
        }
        return text
    }

    fun modify(text: String): String {
        val useProvider = provider
        return if(useProvider != null){
            modifyWithProvider(text, useProvider)
        }else{
            modifyByConditions(text, conditionsList)
        }
    }
    override fun modify(mutableSpan: MutableSpan, styleParameters: StyleParameters) {
        val modified =  modify(mutableSpan.plain)
        mutableSpan.change(modified)
    }

    override fun modify(record: RenderRecord, styleParameters: StyleParameters) {
        TODO("Not yet implemented")
    }
}

