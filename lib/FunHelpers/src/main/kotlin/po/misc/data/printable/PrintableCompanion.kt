package po.misc.data.printable

import po.misc.collections.StaticTypeKey
import po.misc.data.styles.SpecialChars
import po.misc.functions.dsl.DSLBuilder
import po.misc.functions.dsl.DSLContainer
import po.misc.functions.dsl.dslBuilder
import po.misc.types.getOrManaged
import kotlin.reflect.KClass

abstract class PrintableCompanion<T : PrintableBase<T>>(private val classProvider: ()-> KClass<T>) {

    private var typeKeyBacking: StaticTypeKey<T>? = null
    val typeKey: StaticTypeKey<T> get() = typeKeyBacking.getOrManaged("typeKey")

    val metaDataInitialized: Boolean
        get() = typeKeyBacking != null

    init {
        if(!metaDataInitialized){
            typeKeyBacking = StaticTypeKey.Companion.createTypeKey(classProvider.invoke())
        }
    }

    fun createTemplate(
        separator: String = SpecialChars.NewLine.char,
        block: DSLContainer<T, String>.() -> Unit
    ): Template<T>{
      val template =  Template<T>(separator)
      template.dslBuilder(block)

      return  template
    }

}