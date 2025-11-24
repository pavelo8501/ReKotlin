package po.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.KeyedCellOptions
import po.misc.data.pretty_print.formatters.TextModifier
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.styles.Colour
import po.misc.reflection.Readonly
import po.misc.reflection.resolveProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


interface PrettyDataContainer{
    val prettyCells : List<PrettyCellBase<KeyedPresets>>
}

class ReadOnlyPropertyContainer<T: Any>(val receiver: T): PrettyDataContainer {

    enum class PropertyType{
        KProperty1,
        KProperty0
    }
    var propertyType: PropertyType = PropertyType.KProperty0


    internal val prettyCellsBacking = mutableListOf<PrettyCellBase<KeyedPresets>>()
    override val prettyCells : List<PrettyCellBase<KeyedPresets>> get() = prettyCellsBacking

    internal fun addPropertyCell(property: KProperty<Any>,  options: KeyedCellOptions): KeyedCell {
        val cell = when(property){
            is KProperty1<*, *>->{
                propertyType = PropertyType.KProperty1
                val cell = KeyedCell(property)
                prettyCellsBacking.add(cell)
                cell
            }
            is KProperty0<*>->{
                receiver.resolveProperty(Readonly, property)?.let {
                    propertyType = PropertyType.KProperty1
                    val cell = KeyedCell(it)
                    prettyCellsBacking.add(cell)
                    cell
                }?:run {
                    propertyType = PropertyType.KProperty0
                    val cell = KeyedCell(property)
                    prettyCellsBacking.add(cell)
                    "property ${property.name} can not be cased to KProperty1<* , *>".output(Colour.Yellow)
                    cell
                }
            }
            else -> {
                throw IllegalStateException("property type ${property::class} unsupported")
            }
        }
        return cell.applyOptions(options)
    }

    fun addCell(property: KProperty<Any>, options: KeyedCellOptions = KeyedCellOptions()): KeyedCell {
        return addPropertyCell(property, options)
    }

    fun addCell(property: KProperty<Any>, options: KeyedCellOptions = KeyedCellOptions(), vararg modifiers: TextModifier): KeyedCell {
        val cell = addCell(property, options)
        cell.staticModifiers.addModifiers(modifiers.toList())
        return cell
    }

    fun addCell(property: KProperty<Any>, vararg modifiers: TextModifier): KeyedCell {
        val cell = addCell(property)
        cell.staticModifiers.addModifiers(modifiers.toList())
        return cell
    }

    companion object
}

inline fun <reified T: Any> T.buildPrettyRow(builder: ReadOnlyPropertyContainer<T>.(T)-> Unit): PrettyRow {
    val constructor = ReadOnlyPropertyContainer(this)
    builder.invoke(constructor, this)
    val realRow = PrettyRow(constructor)
    realRow.initType = PrettyRow.InitType.Property
    return realRow
}

