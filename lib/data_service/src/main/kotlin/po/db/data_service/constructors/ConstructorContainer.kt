package po.db.data_service.constructors

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.EntityDTO
import kotlin.collections.mutableMapOf
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class ConstructorContainer <T:  EntityDTO<T,E>,E: LongEntity>(val clazz: KClass<T>, val className: String) {

    var constructors = mutableListOf<MutableMap<String,  KParameter>>()

    var params  = mutableMapOf<String,  KParameter>()

    fun addParam(name: String ,type: KParameter){
        params.putIfAbsent(name, type)
    }

    fun addNewConstructor(){
        constructors.add(params)
        params = mutableMapOf()
    }

    init {
        clazz.constructors.forEach { construct ->
            construct.parameters.forEach { param ->
                addParam(param.name ?: "_", param)
            }
            addNewConstructor()
        }
    }
}

class Constructor{
    fun getDefaultForType(kType: KType): Any? {
        return when (kType.classifier) {
            Int::class -> 0
            String::class -> ""
            Boolean::class -> false
            Long::class -> 0L
            else -> null
        }
    }
    fun <T:  EntityDTO<T,E>,E:LongEntity>instantiateFromClass(clazz : KClass<T>, containerConstructor : ConstructorContainer<T,E> ):T?{
        clazz.constructors.forEach {classConstructor->
            containerConstructor.constructors.firstOrNull { classConstructor.parameters.size == it.size }?.let { appropriate->
                val args = appropriate.map { param ->
                    classConstructor.parameters.first { it.name == param.key } to getDefaultForType(param.value.type)
                }.toMap()
                try {
                    return classConstructor.callBy(args)
                }catch (e: Exception){
                    println(e.message)
                }
            }
        }
        return null
    }
}