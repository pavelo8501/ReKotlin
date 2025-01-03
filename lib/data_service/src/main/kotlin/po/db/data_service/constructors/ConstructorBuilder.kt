package po.db.data_service.constructors

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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


    fun <T: Any>getBlueprint(container : ClassBlueprintBase<T>): ClassBlueprintBase<T>{

        container.className  = container.clazz::simpleName.toString()
        container.qualifiedName = container.clazz.qualifiedName.toString()

         container.clazz.primaryConstructor?.let {
            if(it.parameters.isEmpty()){
                container.setEffectiveConstructor(it)
            }else{
                container.setEffectiveConstructor(it)
                it.parameters.forEach { param ->
                    container.addAsArg(param)
                }
            }
        }
        return container
    }

    fun <T: Any>getArgsForConstructor(
        bluePrint : ClassBlueprintBase<T>,
        overrideDefault : ((name:String?)->Any?)? = null
    ): Map<KParameter, Any?>{

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

}