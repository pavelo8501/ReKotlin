package po.misc.data.pretty_print.formatters

import po.misc.data.styles.Colour
import po.misc.data.styles.applyColour
import kotlin.reflect.KClass


interface TextModifier{
    val priority: Int
    fun modify(text: String): String
}


class StaticModifiers(){

    internal val modifiers: MutableList<TextModifier> = mutableListOf()

    fun addModifiers(textModifiers: List<TextModifier>){
        modifiers.clear()
        val sorted = textModifiers.sortedBy { it.priority }
        modifiers.addAll( sorted )
    }

    fun isModifierPresent(modifierClass: KClass<out  TextModifier>): Boolean{
        return modifierClass in modifiers.map { it::class }
    }

    fun getModifier(modifierClass: KClass<out  TextModifier>):TextModifier?{
        return modifiers.firstOrNull { it::class == modifierClass }
    }

    fun modify(content: String): String{
        var modified = content
        modifiers.forEach {
            modified = it.modify(modified)
        }
        return modified
    }
}

open class TextTrimmer(val maxLength: Int, val applyText: String): TextModifier{
    override val priority: Int = 0

    override fun modify(text: String): String {
        return text.take(maxLength.coerceAtMost(text.length)) + applyText
    }
}

open class ColorModifier(vararg val conditions: ColourCondition): TextModifier{

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

    override fun modify(text: String): String {

        val useProvider = provider
        if(useProvider != null){
            val colour = useProvider.invoke()
            if(colour != null){
               return text.applyColour(colour)
            }
            return text
        }else{
            for (condition in conditionsList){
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
        }
        return text
    }
}


