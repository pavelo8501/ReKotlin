package po.misc.data.pretty_print.parts.options

/**
 * Defines horizontal text alignment inside a pretty-print cell.
 */
enum class Align {
    /** Align text to the left edge of the cell. */
    Left,
    /** Align text to the right edge of the cell. */
    Right,
    /** Center text horizontally inside the cell. */
    Center
}

/**
* Base contract for output environment defaults used by pretty-printing.
*
* Implementations define constraints such as maximum renderable width.
*/
enum class ViewPortSize(val size: Int) {

    /**
     * A wide console environment (220 chars).
     * Suitable for modern terminals with more available horizontal space.
     */
    Console220(220),

    /**
     * A wide console environment (120 chars).
     * Suitable for modern terminals with more available horizontal space.
     */
    Console120(120),

    /**
     * A safe default environment for output with reduced width (80 chars).
     * Suitable for older terminals or compact console views.
     */
    Console80(80),

    Console40(40),

}
