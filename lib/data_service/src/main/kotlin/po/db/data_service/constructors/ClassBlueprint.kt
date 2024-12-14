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

data class ClassBlueprint<T: DAOWInstance>(
    val className: String,
    val clazz : KClass<T>,
){

    var constructorArgs  = mutableListOf<ConstructorArgument>()
        private set

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    private var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0

    fun setEffectiveConstructor(constructor : KFunction<T>){
        effectiveConstructor = constructor
        effectiveConstructorSize = constructor.parameters.size
    }

    fun getEffectiveConstructor(): KFunction<T>{
        return effectiveConstructor?: throw OperationsException("Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }


//    fun addArg(name: String , value: Any?){
//        constructorParams.putIfAbsent(name, value)
//    }

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