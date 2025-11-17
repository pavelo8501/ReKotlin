package po.misc.data.printable.companion

import po.misc.data.json.JsonDescriptor
import po.misc.data.json.JsonDescriptorBase
import po.misc.data.json.models.JsonObject
import po.misc.data.printable.Printable
import po.misc.functions.dsl.DSLConstructor
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken


abstract class PrintableCompanion<T : Printable>(
   override  val typeToken: TypeToken<T>
): Tokenized<T>{

    val metaDataInitialized: Boolean
        get() = true

    internal var jsonDescriptor: JsonDescriptorBase<T>? = null

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