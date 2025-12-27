package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.toProvider
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


class RowBuilder<T>(
    type: TypeToken<T>,
    rowID: RowID? = null
): RowBuilderBase<T, T>(type, type), PrettyHelper {

    constructor(row: PrettyRow<T>) : this(row.typeToken) {
        prettyRow = row
    }

    override var prettyRow: PrettyRow<T> = PrettyRow.createEmpty(type, options, rowID)
        private set

    fun addKeyless(prop: KProperty1<T, Any?>): KeyedCell<T> {
        val opts = Options(CellPresets.KeylessProperty)
        val provider =  prop.toProvider(valueType)
        val cell = KeyedCell(provider, opts)
        return storeCell(cell)
    }


    fun addCell(opt: CommonCellOptions? = null): PrettyCell {
        val options = PrettyHelper.toOptionsOrNull(opt)
        val cell = PrettyCell().applyOptions(options)
        return storeCell(cell)
    }

    fun addCells(vararg property: KProperty0<*>): List<KeyedCell<T>> {
        val cells = property.map { storeCell(KeyedCell(valueType).setSource(it)) }
        return cells
    }

    override fun toString(): String {
        return buildString {
            append("RowBuilder of ${prettyRow.templateData}")
        }
    }
    companion object {
        inline operator fun <reified T> invoke(rowID: RowID? = null):RowBuilder<T> = RowBuilder(TypeToken<T>(), rowID)
    }
}



