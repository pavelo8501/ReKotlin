package po.misc.data.printable

import po.misc.collections.StaticTypeKey
import po.misc.types.getOrManaged
import kotlin.reflect.KClass

abstract class PrintableCompanion<T : PrintableBase<T>>(private val classProvider: ()-> KClass<T>) {


    private var typeKeyBacking: StaticTypeKey<T>? = null
    val  typeKey: StaticTypeKey<T> get() = typeKeyBacking.getOrManaged("typeKey")

    val metaDataInitialized: Boolean
        get() = typeKeyBacking != null

    init {
        if(!metaDataInitialized){
            typeKeyBacking = StaticTypeKey.Companion.createTypeKey(classProvider.invoke())
        }
    }
}