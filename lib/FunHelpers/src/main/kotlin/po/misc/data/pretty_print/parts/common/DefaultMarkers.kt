package po.misc.data.pretty_print.parts.common

import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.RowID


internal enum class Grid: GridID { Grid1, Grid2, ForeignGrid, SubTemplateGrid }

internal enum class Row: RowID { Row1, Row2, Row3,  SubTemplateRow }