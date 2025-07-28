package po.misc.data.printable.companion

import po.misc.collections.StaticTypeKey
import po.misc.data.printable.PrintableBase
import po.misc.functions.dsl.DSLConstructor
import po.misc.types.getOrManaged
import kotlin.reflect.KClass

abstract class PrintableCompanion<T : PrintableBase<T>>(private val classProvider: ()-> KClass<T>) {
    private var typeKeyBacking: StaticTypeKey<T>? = null
    val printableClass: KClass<T> by lazy { classProvider() }
    val typeKey: StaticTypeKey<T> get() = typeKeyBacking.getOrManaged("typeKey")

    val metaDataInitialized: Boolean
        get() = typeKeyBacking != null

    init {
        if(!metaDataInitialized){
            typeKeyBacking = StaticTypeKey.Companion.createTypeKey(classProvider.invoke())
        }
    }

    val templates : MutableList<PrintableTemplateBase<T>>  = mutableListOf()

    fun createTemplate(dslLambda: DSLConstructor<T, String>.()-> Unit): Template<T> {
        val template = Template<T>()
        template.dslConstructor.build(dslLambda)
        templates.add(template)
        return template
    }

}