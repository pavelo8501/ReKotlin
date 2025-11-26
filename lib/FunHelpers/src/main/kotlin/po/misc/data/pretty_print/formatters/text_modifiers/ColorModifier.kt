package po.misc.data.pretty_print.formatters.text_modifiers


import po.misc.data.styles.Colour
import po.misc.data.styles.applyColour


open class ColorModifier(vararg val conditions: ColourCondition): TextModifier {
    var provider: (()-> Colour?)? = null

    constructor(colourProvider: ()-> Colour?):this()

    class ColourCondition(
        val match: String,
        val colour: Colour,
        val matchType: Match = Match.Partially
    ){
        enum class Match{Partially, Exactly}
    }
    override val priority: Int = 1
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
    override fun modify(text: String): String {
        val useProvider = provider
        return if(useProvider != null){
            modifyWithProvider(text, useProvider)
        }else{
            modifyByConditions(text, conditionsList)
        }
    }
}

