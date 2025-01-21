package po.db.data_service.constructors

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

data class ClassData(
    val name: String,
    val clazz:KClass<*>
)



open class ConstructorBuilder {

    private fun <T: Any>getNestedMap(clazz: KClass<out T>): Map<String, Map<String,ClassData>>{
        val map = mutableMapOf<String, Map<String,ClassData>>()
        val child = mutableMapOf<String,ClassData>()
        clazz.nestedClasses.forEach {
            child[it.simpleName.toString()] = ClassData(it.simpleName.toString(), it)
            getNestedMap(it)
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
            container : ClassBlueprintBase<T>
    ): ClassBlueprintBase<T>
    {
        container.apply {
            clazz.primaryConstructor?.let {
                if(it.parameters.isEmpty()){
                    setConstructor(it)
                }else{
                    setConstructor(it)
                    it.parameters.forEach { param ->
                        addAsArg(param)
                    }
                }
            }
            setNestedMap(getNestedMap(clazz))
            setPropertyMap(getProperties<T>(clazz))
        }
        return container
    }

}