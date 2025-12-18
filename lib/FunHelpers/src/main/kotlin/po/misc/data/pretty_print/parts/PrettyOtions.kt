package po.misc.data.pretty_print.parts


/**
 * Marker interface for all PrettyGrid configuration options.
 *
 * Implementations provide styling, layout, and rendering behaviour for cells,
 * rows, and grid components. This interface does not define any properties;
 * it simply identifies a type as belonging to the PrettyGrid options system.
 */
sealed interface PrettyOptions{
    fun asOptions(width: Int = 0): Options
}


/**
 * Base marker interface for cell-related options.
 *
 * This interface represents configuration elements shared across multiple
 * cell-option types. It does not define any properties but serves as a
 * logical grouping for cell styling and behaviour.
 */
sealed interface CommonCellOptions : PrettyOptions{
    val style: Style
    val keyStyle : Style
}