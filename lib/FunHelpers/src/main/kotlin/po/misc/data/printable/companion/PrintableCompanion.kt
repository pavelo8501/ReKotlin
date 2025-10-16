package po.misc.data.printable.companion

import po.misc.collections.StaticTypeKey
import po.misc.data.json.JsonDescriptor
import po.misc.data.json.JsonDescriptorBase
import po.misc.data.json.models.JsonObject
import po.misc.data.printable.PrintableBase
import po.misc.functions.dsl.DSLConstructor
import po.misc.types.getOrManaged
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass

abstract class PrintableCompanion<T : PrintableBase<T>>(private val classProvider: ()-> KClass<T>) {
    private var typeKeyBacking: TypeToken<T>? = null
    val printableClass: KClass<T> by lazy { classProvider() }
    val typeKey: TypeToken<T> get() = typeKeyBacking.getOrManaged("typeKey")

    val metaDataInitialized: Boolean
        get() = typeKeyBacking != null

    internal var jsonDescriptor: JsonDescriptorBase<T>? = null

    init {

    }

    val templates : MutableList<PrintableTemplateBase<T>>  = mutableListOf()

    fun createTemplate(dslLambda: DSLConstructor<T, String>.()-> Unit): Template<T> {
        val template = Template<T>()
        template.dslConstructor.build(dslLambda)
        templates.add(template)
        return template
    }

    fun buildJsonDescriptor(builder: JsonObject<T, T>.()-> Unit): JsonDescriptor<T> {
       val descriptor = JsonDescriptor(this, builder)
        jsonDescriptor = descriptor
        descriptor.build()
        return descriptor
    }


//    fun buildJson(dslLambda: DSLConstructor<T, JsonObject<T>>.()-> Unit): JsonDescriptor2<T> {
//        val descriptor = JsonDescriptor2<T>()
//        descriptor.dslConstructor.build(dslLambda)
//        jsonDescriptor = descriptor
//        return descriptor
//    }

}