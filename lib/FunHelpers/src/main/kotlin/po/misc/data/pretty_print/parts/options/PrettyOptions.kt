package po.misc.data.pretty_print.parts.options


/**
 * Marker interface for all PrettyGrid configuration options.
 *
 * Implementations provide styling, layout, and rendering behaviour for cells,
 * rows, and grid components. This interface does not define any properties;
 * it simply identifies a type as belonging to the PrettyGrid options system.
 */
sealed interface PrettyOptions{
    val plainKey: Boolean
    fun asOptions(width: Int = 0): Options
}

/**
 * Base marker interface for cell-related options.
 *
 * This interface represents configuration elements shared across multiple
 * cell-option types. It does not define any properties but serves as a
 * logical grouping for cell styling and behaviour.
 */
sealed interface CellOptions : PrettyOptions{
    val style: Style
    val keyStyle : Style
    val align: Align
    val renderKey:Boolean
}