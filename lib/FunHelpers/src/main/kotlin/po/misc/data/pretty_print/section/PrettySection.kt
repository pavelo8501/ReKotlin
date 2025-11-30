package po.misc.data.pretty_print.section
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.types.token.TypeToken


/**
 * A contract representing a renderable pretty-printing section.
 *
 * A section defines:
 * - a receiver type [T]
 * - a collection of rows ([prettyRows])
 * - an optional identification tag for selective rendering
 *
 * Implementations (such as [po.misc.data.pretty_print.grid.PrettyGrid] and [PrettyTemplate]) provide
 * the logic for assembling rows and performing the rendering.
 *
 * @param T the type of object this section renders.
 */
interface PrettySection<T: Any>{
    /**
     * Runtime type information used for reflective property access inside rows.
     */
    val typeToken: TypeToken<T>

    /**
     * id label that can be used in selective rendering,
     * e.g., via [po.misc.data.pretty_print.parts.RenderOptions].
     */
    val ids: List<Enum<*>>

    /**
     * Rows of the section, in the order they will be rendered.
     */
    val prettyRows: List<PrettyRow<*>>

    /**
     * Renders the given [receiver] to a formatted multi-line string.
     *
     * @param receiver the object used to populate rows during rendering
     * @param options optional rendering configuration
     */
    fun render(receiver: T, renderOptions: RowRender? = null): String
}