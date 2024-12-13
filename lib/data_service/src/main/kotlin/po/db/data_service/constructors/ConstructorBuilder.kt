package po.db.data_service.constructors

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import po.db.data_service.dto.DataModel
import kotlin.collections.mutableMapOf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

data class ClassBlueprint<T: Any>(
    val className: String,
    val clazz : KClass<T>
){
    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    var effectiveConstructor : KFunction<T>? = null

//    fun addArg(name: String , value: Any?){
//        constructorParams.putIfAbsent(name, value)
//    }

    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }

    fun addParam(param: KParameter , value: Any?){
        constructorParams.putIfAbsent(param, value)
    }
}

object ConstructorBuilder {
     private  fun getDefaultForType(kType: KType): Any? {
         return when (kType.classifier) {
            Int::class -> 0
            String::class -> ""
            Boolean::class -> false
            Long::class -> 0L
            LocalDateTime::class -> nowTime()
            else -> null
        }
    }

    private fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun <T: DataModel>getConstructorBlueprint(clazz: KClass<T>):ClassBlueprint<T>{
        val newBlueprint = ClassBlueprint((clazz.qualifiedName?: clazz::simpleName).toString(), clazz)
        if (clazz.constructors.isNotEmpty()){
            val constructor = clazz.constructors.first()

            val args = constructor.parameters.associateWith {  if(it.type.isMarkedNullable) { null }else{ getDefaultForType(it.type)} }

            args.forEach { (param, value) ->
                println("Param: ${param.name}, Type: ${param.type}, Value: $value")
            }
            newBlueprint.setParams(args)

            constructor.parameters.forEach { param ->
              //  newBlueprint.addParam(param.name ?: "_",  param.isOptional ?: getDefaultForType(param.type) )
            }
            newBlueprint.effectiveConstructor = constructor
        }
        return newBlueprint
    }
}