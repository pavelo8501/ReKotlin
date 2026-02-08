package po.misc.data.pretty_print.parts.decorator

import po.misc.data.pretty_print.parts.common.ExtendedString
import po.misc.data.pretty_print.parts.decorator.DecoratorBorder.BorderRender
import po.misc.data.styles.StyleCode
import po.misc.data.text_span.TextSpan


/**
 * DSL-style configurator for building a composite separator rendering scheme.
 *
 * A separator scheme consists of one or more [po.misc.data.pretty_print.parts.common.Separator] elements that are rendered
 * sequentially to produce the final border text. Implementations may treat the scheme
 * as mutable and stateful.
 *
 * Typical usage:
 * ```
 * separator {
 *     setScheme("-", repeat = 10)
 *     add("+", repeat = 1)
 * }
 * ```
 *
 * Notes:
 * - Most mutating operations are no-ops if no initial separator exists.
 * - Methods are designed for fluent chaining.
 */

sealed interface RenderSchemeBuilder{
    val displaySize: Int

    /**
     * Replaces the current scheme using the provided [po.misc.data.pretty_print.parts.common.ExtendedString].
     *
     * Implementations may choose to clone or reuse the underlying representation.
     */
    fun initBy(extendedString: ExtendedString):RenderSchemeBuilder

    /**
     * Clears the current scheme and initializes it with a newly created [po.misc.data.pretty_print.parts.common.Separator].
     *
     * @param text text used for rendering
     * @param repeat number of times the separator will be rendered
     * @param styleCode optional styling applied to the rendered output
     */
    fun setScheme(text:String, repeat: Int, styleCode: StyleCode? = null):RenderSchemeBuilder

    /**
     * Convenience overload that initializes the scheme from an [ExtendedString].
     */
    fun setScheme(extended: ExtendedString, repeat: Int):RenderSchemeBuilder{
        return setScheme(extended.text, repeat, extended.styleCode)
    }

    /**
     * Appends a new [po.misc.data.pretty_print.parts.common.Separator] to the current scheme.
     * If no separator exists, this call is ignored.
     * Intended to be used after [setScheme].
     *
     * @param separatorText text used for rendering
     * @param repeat number of times the separator will be rendered
     * @param styleCode optional styling applied to the rendered output
     */
    fun append(separatorText:String, repeat: Int, styleCode: StyleCode? = null):RenderSchemeBuilder
    fun append(separatorText:String,  styleCode: StyleCode?, repeat: Int = 1):RenderSchemeBuilder{
        return append(separatorText, 1, styleCode)
    }
    fun append(extended: ExtendedString):RenderSchemeBuilder{
        return append(extended.text, 1,  extended.styleCode)
    }

    /**
     * Prepends a new [po.misc.data.pretty_print.parts.common.Separator] before the last added separator.
     *
     * If no separator exists, this call is ignored.
     * Intended to be used after [setScheme].
     *
     * @param separatorText text used for rendering
     * @param repeat number of times the separator will be rendered
     * @param styleCode optional styling applied to the rendered output
     */
    fun prepend(separatorText:String, repeat: Int, styleCode: StyleCode?  = null):RenderSchemeBuilder
    fun buildRender(buildAction: RenderBuilder.()-> BorderRender): TextSpan
}
