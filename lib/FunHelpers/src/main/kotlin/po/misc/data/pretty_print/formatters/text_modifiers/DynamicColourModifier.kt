package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.styles.Colour
import po.misc.data.styles.applyColour
import po.misc.types.token.TypeToken


interface Condition<T>{
    val colour: Colour
    fun check(value:T):Boolean
}


//class DynamicColourCondition(
//    override val colour: Colour,
//    val condition : String.()-> Boolean
//): Condition<String>{
//
//    override fun check(value:String):Boolean{
//        return condition.invoke(value)
//    }
//}


/**
 * A single dynamic colour rule used by [DynamicColourModifier].
 *
 * @property colour The colour applied if the condition matches.
 * @property condition A predicate executed on the *rendered string value*.
 *
 * Example:
 * ```
 * DynamicColourCondition(
 *     Colour.Cyan,
 *     condition = { startsWith("INFO") }
 * )
 * ```
 */
class ColourCondition<T>(
    override val colour: Colour,
    val condition : T.()-> Boolean
): Condition<T>{
    override fun check(value:T):Boolean{
        return condition.invoke(value)
    }
}


/**
 * A text modifier that applies colours dynamically based on user-defined conditions.
 *
 * `DynamicColourModifier` holds a list of [`DynamicColourCondition`](DynamicColourModifier.DynamicColourCondition)
 * where each condition declares **which colour to apply** and **when to apply it**.
 *
 * Conditions are defined using a simple DSL:
 *
 * ```
 * val cell = PrettyCell().colourConditions {
 *     Colour.Green.buildCondition { contains("success") }
 *     Colour.Red.buildCondition { contains("error") }
 * }
 *
 * cell.render("success: task completed")
 * // → coloured green
 * ```
 *
 * The first matching condition determines the applied colour.
 *
 * Typical usage:
 * - Attach the modifier to a cell via `colourConditions { … }`
 * - Add one or more conditions using the `Colour.buildCondition { … }` builder
 * - Render text and let the modifier colourize it automatically
 *
 * Behaviour notes:
 * - `match(text)` returns true if **any** condition matches.
 * - `modify(text)` colours text using the **first** matching condition.
 * - Conditions operate on `String`, but `Any` values are accepted and internally converted to string.
 *
 * @property conditions initial set of colour–predicate pairs
 */
class DynamicColourModifier<T>(
    override val type: TypeToken<T>,
    vararg val conditions: ColourCondition<T>
): ConditionalTextModifier<T> {

    override val formatter : Formatter = Formatter.ColorModifier

    private val conditionsList: MutableList<ColourCondition<T>> = mutableListOf()
    init { conditionsList.addAll(conditions.toList()) }

    /**
     * Adds a new colour condition.
     *
     * @param colour The colour to apply if the condition matches.
     * @param condition A predicate evaluated against the text being rendered.
     * @return This modifier for chaining.
     *
     * Example:
     * ```
     * modifier.addCondition(Colour.Red) {
     *     contains("ERROR")
     * }
     * ```
     */
    fun  addCondition(colour: Colour, condition : T.()-> Boolean):DynamicColourModifier<T>{
        conditionsList.add(ColourCondition<T>(colour, condition))
        return this
    }

    /**
     * Adds a previously constructed [ColourCondition].
     * @return This modifier for chaining.
     */
    fun addCondition(condition: ColourCondition<T>):DynamicColourModifier<T>{
        conditionsList.add(condition)
        return this
    }


    /**
     * DSL builder used to attach a new [ColourCondition] to the modifier.
     *
     * This is the primary way conditions are defined:
     *
     * ```
     * Colour.Green.buildCondition {
     *     contains("Text_1")
     * }
     *
     * Colour.Magenta.buildCondition {
     *     contains("Text_2")
     * }
     * ```
     *
     * @receiver The colour to apply when the condition matches.
     * @param condition The predicate evaluated against the text being rendered.
     * @return This modifier (not a new one), enabling fluent chaining.
     */
    fun  Colour.buildCondition(condition : T.()-> Boolean):DynamicColourModifier<T>{
        conditionsList.add(ColourCondition(this, condition))
        return this@DynamicColourModifier
    }


    /**
     * Applies the first matching colour condition to the given text.
     *
     * Conditions are evaluated in the order they were added.
     *
     * @param text The input text.
     * @return Coloured text if a condition matched; original text otherwise.
     */
    override fun modify(text: String): String {
        return text
    }

    override fun modify(text: String, parameter: T): String {
        for (dynamicCondition in conditionsList){
            val match: Boolean = dynamicCondition.check(parameter)
            if(match){
                return text.applyColour(dynamicCondition.colour)
            }
        }
        return text
    }

    companion object{
        inline operator fun <reified T> invoke(vararg conditions: ColourCondition<T>): DynamicColourModifier<T> {
           return DynamicColourModifier(TypeToken<T>(), *conditions)
        }
    }
}
