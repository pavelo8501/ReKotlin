package po.exposify.common.classes

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassData
import po.exposify.common.classes.ConstructorBuilder
import po.exposify.common.interfaces.BlueprintContainer
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.dto.CommonDTO
import kotlin.collections.mapOf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType


data class ConstructorArgument(
    val name:String,
    val type: KType,
    val nullable : Boolean
)


class ClassBlueprint<T>(
   private val clazz  : KClass<T>
): BlueprintContainer<T> where T: Any {
    var  paramLookupFn : ( (type: KParameter) -> Any? )? = null

    fun setExternalParamLookupFn(fn : (type: KParameter) -> Any? ){
        paramLookupFn = fn
    }

    private var constructorArgs: MutableList<ConstructorArgument> = mutableListOf<ConstructorArgument>()


    var nestedClasses: Map<String, Map<String, ClassData<*>>> = mapOf<String,  Map<String, ClassData<*>>>()
        private set

    var propertyMap = mapOf<String, KProperty1<T, *>>()
        private set

    protected var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0

    lateinit var constructorBuilder : ConstructorBuilder

    override fun getConstructor(): KFunction<T> {
        return effectiveConstructor?:
        throw OperationsException("Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }


    override fun initialize(builder : ConstructorBuilder){
        constructorBuilder = builder
        builder.getBlueprint(clazz, this)
    }

    override fun getClass(): KClass<T> {
        return clazz
    }


    override fun setConstructor(constructor : KFunction<T>){
        effectiveConstructor = constructor
        effectiveConstructorSize = constructor.parameters.size
    }

    override fun addParameter(param: KParameter) {
        val paramName = param.name ?: "_"
        constructorArgs.add(ConstructorArgument(paramName, param.type, param.isOptional) )
    }

    override fun <N:Any>setNestedMap(map: Map<String, Map<String, ClassData<N>>>) {
        nestedClasses = map
    }

    override fun setPropertyMap(map: Map<String, KProperty1<T, *>>) {
        propertyMap = map
    }



    override fun getArgsForConstructor(): Map<KParameter, Any?> {
        getConstructor().let { constructor ->
            val args =  constructor.parameters.associateWith { param ->
                constructorBuilder.let { builder->
                    if(param.type.isMarkedNullable) {
                        null
                    }else {
                        builder.getDefaultForType(param.type) ?: run {
                            paramLookupFn?.invoke(param)

                        }
                    } }
                addParameter(param)
            }
            return args
        }
    }
}

