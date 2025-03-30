package po.exposify.common.classes

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import po.exposify.common.interfaces.BlueprintContainer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

data class ClassData<T>(
    val name: String,
    val clazz:KClass<out T>
) where  T: Any


open class ConstructorBuilder {

    private fun <T: Any>getNestedMap(clazz: KClass<out T>): Map<String, Map<String,ClassData<Any>>>{
        val map = mutableMapOf<String, Map<String,ClassData<Any>>>()
        val child = mutableMapOf<String,ClassData<Any>>()
        clazz.nestedClasses.forEach {nestedClass->
            nestedClass
            child[nestedClass.simpleName.toString()] = ClassData<Any>(nestedClass.simpleName.toString(), nestedClass)
            getNestedMap(nestedClass)
        }
        map[clazz.simpleName.toString()] = child
        return map
    }

    private fun <T: Any> getProperties(clazz: KClass<T>):Map<String,  KProperty1<T, *>>{
        val propertyMap = mutableMapOf<String,  KProperty1<T, *>>()
        clazz.memberProperties.forEach { prop ->
            propertyMap[prop.name] = prop

        }
        return propertyMap
    }

    fun getDefaultForType(kType: KType): Any? {
         return when (kType.classifier) {
            Int::class -> 0
            String::class -> ""
            Boolean::class -> false
            Long::class -> 0L
            LocalDateTime::class -> {
                LocalDateTime.Companion.parse(
                    Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
                )
            }
            else -> null
        }
    }

    fun <T: Any>getBlueprint(
            clazz : KClass<T>,
            container : BlueprintContainer<T>
    ) { container.apply {
            clazz.primaryConstructor?.let {
                if(it.parameters.isEmpty()){
                    setConstructor(it)
                }else{
                    setConstructor(it)
                    it.parameters.forEach { param ->
                        addParameter(param)
                    }
                }
            }
            setNestedMap(getNestedMap(clazz))
            setPropertyMap(getProperties<T>(clazz))
        }
    }

}