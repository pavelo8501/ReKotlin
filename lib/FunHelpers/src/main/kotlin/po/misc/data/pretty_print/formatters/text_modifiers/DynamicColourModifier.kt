package po.misc.data.pretty_print.formatters.text_modifiers


import po.misc.data.styles.Colour
import po.misc.data.styles.applyColour

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
class DynamicColourModifier(
    vararg val conditions: DynamicColourCondition
): TextModifier {

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
    class DynamicColourCondition(
        val colour: Colour,
        val condition : String.()-> Boolean
    )

    override val priority: Int = 1
    private val conditionsList: MutableList<DynamicColourCondition> = mutableListOf()
    init {
        conditionsList.addAll(conditions.toList())
    }

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
    fun addCondition(colour: Colour, condition : String.()-> Boolean):DynamicColourModifier{
        conditionsList.add(DynamicColourCondition(colour, condition))
        return this
    }

    /**
     * Adds a previously constructed [DynamicColourCondition].
     * @return This modifier for chaining.
     */
    fun addCondition(condition: DynamicColourCondition):DynamicColourModifier{
        conditionsList.add(condition)
        return this
    }

    /**
     * DSL builder used to attach a new [DynamicColourCondition] to the modifier.
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
    fun Colour.buildCondition(condition : String.()-> Boolean):DynamicColourModifier{
        DynamicColourCondition(this, condition)
        conditionsList.add(DynamicColourCondition(this, condition))
        return this@DynamicColourModifier
    }

    /**
     * Checks whether *any* condition matches the given text.
     *
     * @param text The input text.
     * @return `true` if at least one condition evaluates to `true`.
     */
    override fun match(text: String): Boolean {
        val result = conditionsList.any { it.condition.invoke(text) }
        return result
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
        for (dynamicCondition in conditionsList){
            val match: Boolean = dynamicCondition.condition.invoke(text)
            if(match){
                return text.applyColour(dynamicCondition.colour)
            }
        }
        return text
    }
}

