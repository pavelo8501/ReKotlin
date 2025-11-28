package po.misc.data.pretty_print.section

import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.rows.PrettyRowBase
import po.misc.types.token.TypeToken


/**
 * A lightweight wrapper around a [PrettyGrid], exposing it as a reusable pretty-printing section.
 *
 * `PrettyTemplate` is primarily used for defining reusable grid fragments
 * that can be plugged into other grids or templates.
 *
 * The template:
 * - delegates rendering to its underlying [prettyGrid]
 * - propagates [identification] into the grid automatically
 * - exposes the same row list as the underlying grid
 *
 * @param T the type rendered by this template
 * @param typeToken runtime type information used by rows
 * @param prettyGrid the underlying row container
 * @param identification optional identifier associated with this template
 */
open class PrettyTemplate<T: Any>(
    override val typeToken: TypeToken<T>,
    val prettyGrid: PrettyGrid<T>,
    override val identification: Enum<*>? = null
):PrettySection<T> {
    override val prettyRows: List<PrettyRowBase> get() = prettyGrid.prettyRows

    init {
        prettyGrid.identification = identification
    }

    /**
     * Delegates rendering to the underlying [prettyGrid].
     */
    override fun render(receiver: T, options: CommonRenderOptions?): String {
        return prettyGrid.render(receiver, options)
    }
}