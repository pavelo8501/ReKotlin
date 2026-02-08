package po.misc.data.pretty_print.parts.decorator

import po.misc.data.Styled
import po.misc.data.pretty_print.parts.render.CanvasLayer
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.strings.appendGroup
import po.misc.data.strings.appendStyled
import po.misc.data.strings.appendStyledLine
import po.misc.data.text_span.OrderedText
import po.misc.data.text_span.TextSpan

/**
 * Immutable snapshot of a rendered decoration.
 *
 * ### Semantics
 * - `decoration` is a newly composed [TextSpan] representing the final rendered
 *   output. It is built from the original content spans and border separators
 *   and may contain newlines.
 *
 * - `lines` is a structural view of the decoration consisting of individual
 *   [TextSpan] instances:
 *   - border separator spans are newly created
 *   - content spans are **reused and mutated in-place** during decoration
 *
 * ### Important
 * - `width` MUST NOT be derived from `decoration.plainLength`
 * - horizontal size is determined exclusively from `lines`,
 *   which represent the actual rendered footprint
 *
 * @param layer final composed decoration
 * and mutated original content spans
 */
class DecorationContent(
    val name:String,
    val layer: CanvasLayer,
    val snapshot: Decorator.Snapshot
): Styled {

    val status: DecorationStatus = snapshot.status
    val lines: List<TextSpan> = layer.lines
    val contentWidth: Int = layer.lineMaxLen
    override val textSpan: TextSpan get() = layer



    override fun toString(): String {
        return buildString {
            appendStyled("BorderSnapshot[", ::contentWidth, "]")
        }
    }
}