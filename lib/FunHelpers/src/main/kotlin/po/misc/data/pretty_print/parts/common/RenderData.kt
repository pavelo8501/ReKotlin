package po.misc.data.pretty_print.parts.common

import po.misc.data.Styled
import po.misc.data.pretty_print.parts.decorator.DecorationContent
import po.misc.data.pretty_print.parts.rendering.RenderSnapshot
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.TextSpan


/**
 * Width model:
 *
 * - totalXLen       → width explicitly requested for THIS container
 * - usedXLen        → width actually consumed by THIS container after borders
 * - subUsedXLen     → max width consumed by CHILD entries
 * - availableXLen   → remaining horizontal space THIS container may distribute
 *
 * Think of it as:
 * declared → consumed → remaining
 *
 */
class RenderData(
    val snapshot: RenderSnapshot,
    val decorationContent: DecorationContent
): Styled {
    val render: TextSpan get() = decorationContent.render
    val renderedLines : List<TextSpan> get() = decorationContent.renderedLines
    override val textSpan: TextSpan get() = render
    override fun toString(): String {
        return buildString {
            append("Render data. Current renderer name: ${snapshot.rendererName}")
        }
    }
    companion object
}