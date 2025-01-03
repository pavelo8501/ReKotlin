package po.db.data_service.constructors

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.EntityDTO
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

class DataModelBlueprint<DATA: DataModel>(
    clazz : KClass<DATA>
) : ClassBlueprintBase<DATA>(clazz){

    fun getConstructor():KFunction<DATA>{
        return super.getEffectiveConstructor()
    }
}


class EntityBlueprint<ENTITY : LongEntity >(
    clazz : KClass<ENTITY>
) : ClassBlueprintBase<ENTITY>(clazz){
    fun getConstructor():KFunction<ENTITY>{
        return super.getEffectiveConstructor()
    }
}

class DTOBlueprint<DATA, ENTITY>(
    clazz : KClass<DATA>,
    entityClazz: KClass<ENTITY>
) : ClassBlueprintBase<DATA>(clazz) where ENTITY : LongEntity, DATA : DataModel   {
    fun getConstructor():KFunction<DATA>{
        return super.getEffectiveConstructor()
    }
}


abstract class ClassBlueprintBase<T: Any>(
    val clazz : KClass<T>,
){

    var  className: String = ""
    var  qualifiedName : String = ""

    private var constructorArgs  = mutableListOf<ConstructorArgument>()

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