package po.misc.data.pretty_print.parts.decorator

import po.misc.data.Styled
import po.misc.data.strings.appendGroup
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
 * @param render final composed render output
 * @param lines decoration lines; contains both newly created border spans
 * and mutated original content spans
 */
class DecorationContent(
    val name:String,
    val contentWidth: Int,
    val render:TextSpan,
    val renderedLines: List<TextSpan>,
    val snapshot: Decorator.Snapshot
): Styled {

    override val textSpan: TextSpan get() = render
    val status: DecorationStatus = snapshot.status
    val lines: List<String> get() = render.plain.lines()

    fun lines():List<String>{
        return render.styled.lines()
    }

    override fun toString(): String {
        return buildString {
            appendGroup("BorderSnapshot [ ", " ]", ::contentWidth)
        }
    }
}