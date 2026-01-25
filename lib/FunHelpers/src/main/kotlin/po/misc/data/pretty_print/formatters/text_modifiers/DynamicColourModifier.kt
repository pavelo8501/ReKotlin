package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.pretty_print.formatters.DynamicStyleFormatter
import po.misc.data.pretty_print.formatters.FormatterTag
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.text_span.EditablePair
import po.misc.types.token.TypeToken


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
    val colour: Colour,
    val condition : T.()-> Boolean
){
    fun check(value:T):Boolean{
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
): DynamicStyleFormatter<T> {
    override val tag : FormatterTag = FormatterTag.ColorModifier
    private val conditionsBacking: MutableList<ColourCondition<T>> = mutableListOf()
    val conditions : List<ColourCondition<T>> get() =  conditionsBacking

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
    fun  add(colour: Colour, condition : T.()-> Boolean):DynamicColourModifier<T>{
        conditionsBacking.add(ColourCondition<T>(colour, condition))
        return this
    }

    fun  addAll(conditions: List<ColourCondition<T>>):DynamicColourModifier<T>{
        conditionsBacking.addAll(conditions)
        return this
    }

    /**
     * Adds a previously constructed [ColourCondition].
     * @return This modifier for chaining.
     */
    fun add(condition: ColourCondition<T>):DynamicColourModifier<T>{
        conditionsBacking.add(condition)
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
        conditionsBacking.add(ColourCondition(this, condition))
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
    override fun modify(text: String, parameter: T): String {
        for (dynamicCondition in conditions){
            val match: Boolean = dynamicCondition.check(parameter)
            if(match){
                return text.colorize(dynamicCondition.colour)
            }
        }
        return text
    }
    override fun modify(TextSpan: EditablePair, parameter: T) {
        for (dynamicCondition in conditions){
            val match: Boolean = dynamicCondition.check(parameter)
            if(match){
                 val colorized =  TextSpan.plain.colorize(dynamicCondition.colour)
                 TextSpan.writeFormatted(colorized)
            }
        }
    }
    companion object{
        inline operator fun <reified T> invoke(vararg conditions: ColourCondition<T>): DynamicColourModifier<T> {
           return DynamicColourModifier(TypeToken<T>()).addAll(conditions.toList())
        }
    }
}
