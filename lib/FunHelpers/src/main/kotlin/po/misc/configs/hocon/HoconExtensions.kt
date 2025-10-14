package po.misc.configs.hocon

import com.typesafe.config.Config
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.StringClass
import po.misc.reflection.properties.BoolPropertyContainer
import po.misc.reflection.properties.IntPropertyContainer
import po.misc.reflection.properties.LongPropertyContainer
import po.misc.reflection.properties.MutableProperty
import po.misc.reflection.properties.PropertySimpleTypeContainer
import po.misc.reflection.properties.StringPropertyContainer
import po.misc.reflection.properties.toTypedContainer
import po.misc.types.castOrManaged
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

fun <D: Any> Config.mapTo(dataObject:D, keyNameProvider:(String)-> String):D{
    @Suppress("UNCHECKED_CAST")
    val dataObjectClass = dataObject::class as KClass<D>
    val properties =  dataObjectClass.memberProperties

    for(property in properties){
        property.toTypedContainer(dataObject)?.let {container->
            when(container){
                is IntPropertyContainer<D> -> {
                    val value = getInt(keyNameProvider(property.name))
                    container.updateValue(dataObject, value)
                }
                is LongPropertyContainer<D> -> {
                    val value = getLong(keyNameProvider(property.name))
                    container.updateValue(dataObject, value)
                }
                is BoolPropertyContainer<D>->{
                    val value = getBoolean(keyNameProvider(property.name))
                    container.updateValue(dataObject, value)
                }
                is StringPropertyContainer<D> -> {
                    val value = getString(keyNameProvider(property.name))
                    container.updateValue(dataObject, value)
                }
                else -> {
                    "Property is immutable write operation impossible".output(Colour.Yellow)
                }
            }
        }
    }
    return dataObject
}

fun <D: Any> Config.mapToByKeys(
    dataObject: D,
){

    fun warnIfContainerReadOnly(container:  PropertySimpleTypeContainer<D>?) {
        if (container != null) {
            if (!container::class.isSubclassOf(MutableProperty::class)) {
                "Write operation impossible property is read only (val)".output(Colour.Yellow)
            }
        }else{
            "Error creating write safe property container".output(Colour.Red)
        }
    }

    val kClass = dataObject::class.castOrManaged<KClass<D>>(this)
    val properties = kClass.memberProperties.associateBy { it.name  }



    for ((rawKey, value) in entrySet()) {
        val property = properties[rawKey]?: continue
        val classOfValue = property.returnType.classifier as? KClass<*>
        val primitive = PrimitiveClass.ofClass(classOfValue)
        if(primitive != null){
           val container = property.toTypedContainer(dataObject, primitive){
                when(primitive){
                    is StringClass -> {
                        getString(rawKey)
                    }
                    is IntClass -> {
                        getInt(rawKey)
                    }
                    else -> {
                        ""
                    }
                }
           }
           warnIfContainerReadOnly(container)
        }else{
            "$classOfValue not supported".output(Colour.Yellow)
        }
    }
}


