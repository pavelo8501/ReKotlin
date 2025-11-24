package po.misc.data.logging.processor.parts

import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable
import kotlin.reflect.KProperty



class StructuredProperty(
    val options:  StructuredOptions,
){

    constructor(
        template: LoggableTemplate,
        onEntry:(StructuredLoggable)-> Unit
    ):this(StructuredOptions(template, onEntry = onEntry))


    private val structuredList: StructuredList = StructuredList(options)

    var initialName: String
        get() =  structuredList.options?.initialName?:"StructuredProperty"
        set(value) {
            val options = structuredList.options
            if(options != null){
                options.initialName = value
            }
        }

    operator fun provideDelegate(
        thisRef: LoggableTemplate,
        property: KProperty<*>,
    ): StructuredProperty {
        if (initialName.isBlank()) {
            initialName = property.name
        }
        return this
    }

    operator fun getValue(
        thisRef: LoggableTemplate,
        property: KProperty<*>,
    ): StructuredList {
        return structuredList
    }
}

fun LoggableTemplate.structuredProperty(options:  StructuredOptions):StructuredProperty{
    return StructuredProperty(options)
}