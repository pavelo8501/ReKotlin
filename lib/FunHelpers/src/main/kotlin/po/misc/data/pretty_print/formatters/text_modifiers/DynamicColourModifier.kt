package po.misc.data.pretty_print.formatters.text_modifiers


import po.misc.data.styles.Colour
import po.misc.data.styles.applyColour


class DynamicColourModifier(vararg val conditions: DynamicColourCondition): TextModifier {
    class DynamicColourCondition(
        val colour: Colour,
        val condition : String.()-> Boolean
    )
    override val priority: Int = 1
    private val conditionsList: MutableList<DynamicColourCondition> = mutableListOf()

    init {
        conditionsList.addAll(conditions.toList())
    }

    fun addCondition(colour: Colour, condition : String.()-> Boolean):DynamicColourModifier{
        conditionsList.add(DynamicColourCondition(colour, condition))
        return this
    }

    fun addCondition(condition: DynamicColourCondition):DynamicColourModifier{
        conditionsList.add(condition)
        return this
    }

    fun Colour.buildCondition(condition : String.()-> Boolean):DynamicColourModifier{
        DynamicColourCondition(this, condition)
        conditionsList.add(DynamicColourCondition(this, condition))
        return this@DynamicColourModifier
    }

    override fun modify(text: String): String {
        for (dynamicCondition in conditionsList){
            val match: Boolean = dynamicCondition.condition.invoke(text)
            if(match){
                return text.applyColour(dynamicCondition.colour)
            }
        }
        return text
    }
}

