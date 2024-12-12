package po.db.data_service.constructors

import po.db.data_service.dto.DataModel
import kotlin.collections.mutableMapOf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

//class ConstructorContainer <T:  EntityDTO<T,E>,E: LongEntity>(val clazz: KClass<T>, val className: String) {
//
//    var constructors = mutableListOf<MutableMap<String,  KParameter>>()
//
//    var params  = mutableMapOf<String,  KParameter>()
//
//    fun addParam(name: String ,type: KParameter){
//        params.putIfAbsent(name, type)
//    }
//
//    fun addNewConstructor(){
//        constructors.add(params)
//        params = mutableMapOf()
//    }
//
//    init {
//        clazz.constructors.forEach { construct ->
//            construct.parameters.forEach { param ->
//                addParam(param.name ?: "_", param)
//            }
//            addNewConstructor()
//        }
//    }
//}
//

data class ConstructorBlueprint<T: Any>(
    val className: String,
    val clazz : KClass<T>
){
    var constructorParams  = mutableMapOf<String,  KParameter>()
        private set

    var effectiveConstructor : KFunction<T>? = null

    fun addParam(name: String , type: KParameter){
        constructorParams.putIfAbsent(name, type)
    }
}

object ConstructorBuilder {

    fun <T: DataModel>getConstructorBlueprint(clazz: KClass<T>):ConstructorBlueprint<T>{
        val newBlueprint = ConstructorBlueprint((clazz.qualifiedName?: clazz::simpleName).toString(), clazz)
        if (clazz.constructors.isNotEmpty()){
            val constructor = clazz.constructors.first()
            constructor.parameters.forEach { param ->
                newBlueprint.addParam(param.name ?: "_", param)
            }
            newBlueprint.effectiveConstructor = constructor
        }
        return newBlueprint
    }

    fun getDefaultForType(kType: KType): Any? {
        return when (kType.classifier) {
            Int::class -> 0
            String::class -> ""
            Boolean::class -> false
            Long::class -> 0L
            else -> null
        }
    }

//    fun <T:  EntityDTO<T,E>,E:LongEntity>instantiateFromClass(clazz : KClass<T>, containerConstructor : ConstructorContainer<T,E> ):T?{
//        clazz.constructors.forEach {classConstructor->
//            containerConstructor.constructors.firstOrNull { classConstructor.parameters.size == it.size }?.let { appropriate->
//                val args = appropriate.map { param ->
//                    classConstructor.parameters.first { it.name == param.key } to getDefaultForType(param.value.type)
//                }.toMap()
//                try {
//                    return classConstructor.callBy(args)
//                }catch (e: Exception){
//                    println(e.message)
//                }
//            }
//        }
//        return null
//    }

}