package po.misc.data.pretty_print.parts.grid

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.rows.RowParams

class GridSignals<T>(
    val grid: PrettyGridBase<T>
){
    val beforeGridRender: Signal<GridParams, Unit> = signalOf<GridParams, Unit>()
    val beforeRowRender: Signal<RowParams<T>, Unit> = signalOf<RowParams<T>, Unit>()
}