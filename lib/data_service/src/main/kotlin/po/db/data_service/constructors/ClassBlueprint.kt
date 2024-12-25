package po.db.data_service.constructors

import po.db.data_service.dto.interfaces.DAOWInstance
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType


data class ConstructorArgument(
    val name:String,
    val type: KType,
    val nullable : Boolean
)

enum class ConstructorType{
    EMPTY,
    NON_EMPTY
}

data class ClassBlueprint(
    val className: String,
    val clazz : KClass<*>,
){
    private var constructorArgs  = mutableListOf<ConstructorArgument>()

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    private var effectiveConstructor : KFunction<*>? = null
    private var effectiveConstructorSize : Number  = 0

    fun setEffectiveConstructor(constructor : KFunction<*>){
        effectiveConstructor = constructor
        effectiveConstructorSize = constructor.parameters.size
    }

    fun getEffectiveConstructor(): KFunction<*>{
        return effectiveConstructor?: throw OperationsException("Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }

    fun addAsArg(param : KParameter){
        val paramName = param.name ?: "_"
        constructorArgs.add(ConstructorArgument(paramName, param.type, param.isOptional) )
    }

    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }

    fun addParam(param: KParameter, value: Any?){
        constructorParams.putIfAbsent(param, value)
    }
}