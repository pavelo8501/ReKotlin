


class PropertyCell<T, R>(
val property: KProperty1<T, R>,
) : CellRenderBase(...)

class KeyedCell(
val key: String,
) : CellRenderBase(...)

class StaticCell(
val text: String,
) : CellRenderBase(...)

class ComputedCell<T>(
val compute: (T) -> String,
) : CellRenderBase(...)