package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.reflection.Readonly
import po.misc.reflection.resolveProperty
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1



sealed class CellContainerBase<T: Any>(
    val typeToken:  TypeToken<T>,
    val options: RowOptions
){
    enum class PropertyKind{ KProperty1, KProperty0 }

    var propertyKind: PropertyKind = PropertyKind.KProperty0

    internal val prettyCellsBacking = mutableListOf<PrettyCellBase<*>>()
    val cells : List<PrettyCellBase<*>> get() = prettyCellsBacking

    internal fun <T: Any> transformToKProperty1(property: KProperty<T>): KProperty1<Any, T> {

        return when(property){
            is KProperty1<*, *>->{
                propertyKind = PropertyKind.KProperty1
                property.castOrThrow()
            }
            is KProperty0<*>->{
                resolveProperty(Readonly, typeToken,  property)?.let {
                    propertyKind = PropertyKind.KProperty1
                    it.castOrThrow()
                }?:run {
                    propertyKind = PropertyKind.KProperty0
                    throw IllegalStateException("property type ${property::class} unsupported")
                }
            }
            else -> {
                throw IllegalStateException("property type ${property::class} unsupported")
            }
        }
    }

    internal fun <C: PrettyCellBase<*>> storeCell(cell : C): C {
        prettyCellsBacking.add(cell)
        return cell
    }

    internal fun addStaticCell(content: Any, options: CellOptions?): StaticCell{
        val cell = StaticCell(content)
        if(options != null){
            cell.options = options
        }
        storeCell(cell)
        return cell
    }
    fun addCell(content: Any, preset: PrettyPresets):StaticCell = addStaticCell(content, preset.toOptions())

    fun addCell(content: Any, options: CellOptions? = null): StaticCell = addStaticCell(content, options)
    fun addCell(staticCell: StaticCell.Companion, options: CellOptions? = null, builderAction: StringBuilder.() -> Unit):StaticCell{
       val cell = addStaticCell("", options)
       return cell.buildText(builderAction)
    }

    fun <V: Any> addCell(property: KProperty<V>, lambda: ComputedCell<V>.(V)-> Any): ComputedCell<V>{
        val transformed = transformToKProperty1(property)
        val cell = ComputedCell<V>(transformed, lambda)
        return storeCell(cell)
    }

    fun addCell(companion: ComputedCell.Companion,  property: KProperty<T>, lambda: ComputedCell<T>.(T)-> Any): ComputedCell<T>{
        val transformed = transformToKProperty1(property)
        val cell = ComputedCell<T>(transformed, lambda)
        return storeCell(cell)
    }

    open fun addCell(options: CellOptions? = null, lambda: ComputedCell<T>.(T)-> Any): ComputedCell<T>{
        val cell = ComputedCell<T>(lambda)
        return storeCell(cell)
    }

    internal fun addKeyedCell(property: KProperty<Any>, options: KeyedCellOptions?, modifiers: List<TextModifier>?): KeyedCell {
        val transformed = transformToKProperty1(property)
        val cell = KeyedCell(transformed)
        if(options != null){
            cell.applyOptions(options)
        }
        modifiers?.let {
            cell.staticModifiers.addModifiers(it)
        }
        return storeCell(cell)
    }

    fun addCell(property: KProperty<Any>, options: KeyedCellOptions? = null): KeyedCell =
        addKeyedCell(property, options, null)

    fun addCell(property: KProperty<Any>, preset: KeyedPresets): KeyedCell  = addKeyedCell(property, KeyedCellOptions(preset), null)
    fun addCell(property: KProperty<Any>, options: KeyedCellOptions, vararg modifiers: TextModifier): KeyedCell =
        addKeyedCell(property, options, modifiers.toList())
    fun addCell(property: KProperty<Any>, vararg modifiers: TextModifier): KeyedCell =
        addKeyedCell(property, options = null, modifiers = modifiers.toList())

    fun addCells(property: KProperty<Any>, vararg properties: KProperty<Any>, options: KeyedCellOptions? = null): List<KeyedCell>{
        val result = mutableListOf<KeyedCell>()
        val list = buildList {
            add(property)
            addAll(properties.toList())
        }
        list.forEach {
            val cellAdded = addKeyedCell(it, options, null)
            result.add(cellAdded)
        }
        return result
    }
    fun addCells(property: KProperty<Any>, vararg properties: KProperty<Any>, preset: KeyedPresets): List<KeyedCell> =
        addCells(property, *properties, options = KeyedCellOptions(preset))

}

class CellContainer<T: Any>(
    typeToken:  TypeToken<T>,
    options: RowOptions = RowOptions()
): CellContainerBase<T>(typeToken, options){
    companion object
}

class CellReceiverContainer<T: Any>(
    val receiver: T,
    typeToken:  TypeToken<T>,
    options: RowOptions = RowOptions()
): CellContainerBase<T>(typeToken, options){

    companion object
}



