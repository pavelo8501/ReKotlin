package po.misc.data.pretty_print.parts

/**
 * Defines horizontal text alignment inside a pretty-print cell.
 */
enum class Align {
    /** Align text to the left edge of the cell. */
    LEFT,
    /** Align text to the right edge of the cell. */
    RIGHT,
    /** Center text horizontally inside the cell. */
    CENTER
}

/**
 * Base contract for output environment defaults used by pretty-printing.
 *
 * Implementations define constraints such as maximum renderable width.
 */
sealed interface RenderDefaults{
    val defaultWidth : Int
}

/**
 * A safe default environment for output with reduced width (80 chars).
 *
 * Suitable for older terminals or compact console views.
 */
object Console80 : RenderDefaults {
    override val defaultWidth : Int = 80
}

/**
 * A wide console environment (120 chars).
 *
 * Suitable for modern terminals with more available horizontal space.
 */
object Console120 : RenderDefaults {
    override val defaultWidth : Int = 120
}

object Console220 : RenderDefaults {
    override val defaultWidth : Int = 220
}