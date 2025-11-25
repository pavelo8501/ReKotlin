package po.misc.data.pretty_print.rows

import po.misc.data.output.output
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.styles.Colour
import po.misc.reflection.Readonly
import po.misc.reflection.resolveProperty
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


interface PrettyDataContainer{
    val prettyCells : List<PrettyCellBase<*>>
}

sealed class  CellContainerBase<T: Any>(
    val typeToken:  TypeToken<T>
): PrettyDataContainer{
    enum class PropertyKind{ KProperty1, KProperty0 }

    var propertyKind: PropertyKind = PropertyKind.KProperty0

    internal val prettyCellsBacking = mutableListOf<PrettyCellBase<*>>()
    override val prettyCells : List<PrettyCellBase<*>> get() = prettyCellsBacking

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

    fun <C: PrettyCellBase<*>> storeCell(cell : C):C{
        prettyCellsBacking.add(cell)
        return cell
    }

    //builderAction: StringBuilder.() -> Unit

    internal fun addStaticCell(
        content: Any,
        options: CommonCellOptions? = null
    ): StaticCell{
        val cell = StaticCell(content)
        if(options != null){
            cell.options = options
        }
        return storeCell(cell)
    }
    fun addCell(content: Any, options: CommonCellOptions? = null): StaticCell = addStaticCell(content, options)
    fun addCell(
        staticCell: StaticCell.Companion,
        options: CommonCellOptions? = null,
        builderAction: StringBuilder.() -> Unit
    ):StaticCell{
       val cell = addStaticCell("", options)
       return cell.buildText(builderAction)
    }

    fun <T: Any> addCell(property: KProperty<T>,  lambda: ComputedCell<T>.(T)-> Any):ComputedCell<T>{
        val transformed = transformToKProperty1(property)
        val cell = ComputedCell<T>(20,  transformed, lambda)
        return storeCell(cell)
    }

    internal fun addKeyedCell(
        property: KProperty<Any>,
        options: KeyedCellOptions? = null
    ): KeyedCell {
        val transformed = transformToKProperty1(property)
        val cell = KeyedCell(transformed)
        if(options != null){
            cell.applyOptions(options)
        }
        return storeCell(cell)
    }
    fun addCell(property: KProperty<Any>, options: KeyedCellOptions? = null): KeyedCell = addKeyedCell(property, options)
    fun addCell(property: KProperty<Any>, preset: KeyedPresets): KeyedCell  = addKeyedCell(property, KeyedCellOptions(preset))

    fun addCell(property: KProperty<Any>, options: KeyedCellOptions, vararg modifiers: TextModifier): KeyedCell {
        val transformed = transformToKProperty1(property)
        val cell = KeyedCell(transformed)
        cell.staticModifiers.addModifiers(modifiers.toList())
        cell.applyOptions(options)
        cell.staticModifiers.addModifiers(modifiers.toList())
        return cell
    }

    fun addCell(property: KProperty<Any>, vararg modifiers: TextModifier): KeyedCell =
        addCell(property, options = KeyedCellOptions(), modifiers = modifiers)
}

class CellContainer<T: Any>(typeToken:  TypeToken<T>): CellContainerBase<T>(typeToken){
    companion object
}

class CellReceiverContainer<T: Any>(val receiver: T, typeToken:  TypeToken<T>): CellContainerBase<T>(typeToken){
    companion object
}

//
//class ReadOnlyPropertyContainer<T: Any>(val receiver: T): PrettyDataContainer {
//    enum class PropertyType{
//        KProperty1,
//        KProperty0
//    }
//    var propertyType: PropertyType = PropertyType.KProperty0
//
//    internal val prettyCellsBacking = mutableListOf<PrettyCellBase<*>>()
//    override val prettyCells : List<PrettyCellBase<*>> get() = prettyCellsBacking
//
//    internal fun addPropertyCell(property: KProperty<Any>,  options: KeyedCellOptions): KeyedCell {
//        val cell = when(property){
//            is KProperty1<*, *>->{
//                propertyType = PropertyType.KProperty1
//                val cell = KeyedCell(property)
//                prettyCellsBacking.add(cell)
//                cell
//            }
//            is KProperty0<*>->{
//                receiver.resolveProperty(Readonly, property)?.let {
//                    propertyType = PropertyType.KProperty1
//                    val cell = KeyedCell(it)
//                    prettyCellsBacking.add(cell)
//                    cell
//                }?:run {
//                    propertyType = PropertyType.KProperty0
//                    val cell = KeyedCell(property)
//                    prettyCellsBacking.add(cell)
//                    "property ${property.name} can not be cased to KProperty1<* , *>".output(Colour.Yellow)
//                    cell
//                }
//            }
//            else -> {
//                throw IllegalStateException("property type ${property::class} unsupported")
//            }
//        }
//        return cell.applyOptions(options)
//    }
//
//    fun addCell(content: Any): StaticCell {
//        val cell =  StaticCell(content)
//        prettyCellsBacking.add(cell)
//        return cell
//    }
//
//    fun addCell(key: String, property: KProperty<Any>): KeyedCell {
//        return addPropertyCell(property, KeyedCellOptions(useKeyName = key))
//    }
//
//    fun addCell(property: KProperty<Any>, options: KeyedCellOptions = KeyedCellOptions()): KeyedCell {
//        return addPropertyCell(property, options)
//    }
//
//    fun addCell(property: KProperty<Any>, options: KeyedCellOptions = KeyedCellOptions(), vararg modifiers: TextModifier): KeyedCell {
//        val cell = addCell(property, options)
//        cell.staticModifiers.addModifiers(modifiers.toList())
//        return cell
//    }
//
//    fun addCell(property: KProperty<Any>, vararg modifiers: TextModifier): KeyedCell {
//        val cell = addCell(property)
//        cell.staticModifiers.addModifiers(modifiers.toList())
//        return cell
//    }
//    companion object
//}

//inline fun <reified T: Any> T.buildPrettyRow2(builder: ReadOnlyPropertyContainer<T>.(T)-> Unit): PrettyRow {
//    val constructor = ReadOnlyPropertyContainer(this)
//    builder.invoke(constructor, this)
//    val realRow = PrettyRow(constructor)
//    return realRow
//}


inline fun <reified T: Any> T.buildPrettyRow(builder: CellReceiverContainer<T>.(T)-> Unit): PrettyRow {
    val constructor = CellReceiverContainer<T>(this, TypeToken.create())
    builder.invoke(constructor, this)
    val realRow = PrettyRow(constructor)
    return realRow
}

inline fun <reified T: Any> T.buildPrettyRow(
    container: CellReceiverContainer.Companion,
    rowOptions: RowOptions? = null,
    noinline builder: CellReceiverContainer<T>.(T)-> Unit
): PrettyRow =  PrettyGrid.createPrettyRowBuilding(this, TypeToken.create<T>(), rowOptions,  builder)


fun <T: Any> T.buildPrettyRow(
    container: CellReceiverContainer.Companion,
    typeToken: TypeToken<T>,
    rowOptions: RowOptions? = null,
    builder: CellReceiverContainer<T>.(T)-> Unit
): PrettyRow = PrettyGrid.createPrettyRowBuilding(this, typeToken, rowOptions,  builder)


inline fun <reified T: Any> buildPrettyRow(
    rowOptions: RowOptions? = null,
    noinline builder: CellContainer<T>.()-> Unit
): PrettyRow =  PrettyGrid.createPrettyRowBuilding(TypeToken.create<T>(), rowOptions, builder)

inline fun <reified T: Any> buildPrettyRow(
    container: CellContainer.Companion,
    rowOptions: RowOptions? = null,
    noinline builder: CellContainer<T>.()-> Unit
): PrettyRow = PrettyGrid.createPrettyRowBuilding(TypeToken.create<T>(), rowOptions,  builder =  builder)

fun <T: Any> buildPrettyRow(
    container: CellContainer.Companion,
    typeToken: TypeToken<T>,
    rowOptions: RowOptions? = null,
    builder: CellContainer<T>.()-> Unit
): PrettyRow = PrettyGrid.createPrettyRowBuilding(typeToken, rowOptions, builder)



