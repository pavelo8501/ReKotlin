package po.db.data_service.constructors

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import po.db.data_service.dto.interfaces.DAOWInstance
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor


abstract class ConstructorBuilder {
     private  fun getDefaultForType(kType: KType): Any? {
         return when (kType.classifier) {
            Int::class -> 0
            String::class -> ""
            Boolean::class -> false
            Long::class -> 0L
            LocalDateTime::class -> LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
            else -> null
        }
    }

    fun <T: Any>getConstructorBlueprint(clazz: KClass<*>):ClassBlueprint{

        val className = (clazz.qualifiedName?: clazz::simpleName).toString()
        val newBlueprint = ClassBlueprint(className, clazz)
        clazz.primaryConstructor?.let {
            if(it.parameters.isEmpty()){
                newBlueprint.setEffectiveConstructor(it)
            }else{
                newBlueprint.setEffectiveConstructor(it)
                it.parameters.forEach { param ->
                    newBlueprint.addAsArg(param)
                }
            }
        }
        return newBlueprint
    }

    fun getArgsForConstructor(bluePrint : ClassBlueprint, overrideDefault : ((name:String?)->Any?)? = null): Map<KParameter, Any?>{
        bluePrint.getEffectiveConstructor().let { constructor ->
           val args = constructor.parameters.associateWith { param ->
                if(param.type.isMarkedNullable) {
                    null
                }else{
                   val result =  if(overrideDefault == null) {
                        getDefaultForType(param.type)
                    }else{
                      overrideDefault.invoke(param.name)?:getDefaultForType(param.type)
                    }
                    result
                }
            }
            bluePrint.setParams(args)
            return args
        }
    }

//    fun <T: DataModel>getConstructorBlueprint(clazz: KClass<T>):ClassBlueprint<T>{
//        val className = (clazz.qualifiedName?: clazz::simpleName).toString()
//        val newBlueprint = ClassBlueprint(className, clazz)
//        if (clazz.constructors.isNotEmpty()){
//            val constructor = clazz.constructors.first()
//            val args = constructor.parameters.associateWith {if(it.type.isMarkedNullable) { null }else{ getDefaultForType(it.type)} }
//
//            args.forEach { (param, value) ->
//                println("Param: ${param.name}, Type: ${param.type}, Value: $value")
//            }
//            newBlueprint.setParams(args)
//
//            constructor.parameters.forEach { param ->
//              newBlueprint.addAsArg(param)
//            }
//            newBlueprint.effectiveConstructor = constructor
//        }
//        return newBlueprint
//    }
}