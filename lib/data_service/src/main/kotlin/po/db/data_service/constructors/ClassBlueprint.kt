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

    override fun getClass(): KClass<DATA> {
        return getSourceClass()
    }
}


class EntityBlueprint<ENTITY : LongEntity >(
    clazz : KClass<ENTITY>
) : ClassBlueprintBase<ENTITY>(clazz){

    override fun getClass(): KClass<ENTITY> {
       return getSourceClass()
    }

}

class DTOBlueprint<DATA, ENTITY>(
    override val clazz  : KClass<out EntityDTO<DATA, ENTITY>>,
) : CovariantClassBlueprintBase< EntityDTO<DATA,ENTITY>>() where ENTITY : LongEntity, DATA : DataModel



abstract class CovariantClassBlueprintBase<T: Any>(): ClassBlueprintBase<T>(){

    abstract val clazz : KClass<out T>

    override fun getConstructor(): KFunction<T>{
        return effectiveConstructor?: throw OperationsException(
            "Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }

    override fun getClass():KClass<out T>{
        return clazz
    }

}



abstract class ClassBlueprintBase<T: Any>(inputClazz : KClass<T>? = null){

    private val forClass : KClass<T>? = inputClazz

    var  className: String = ""
    var  qualifiedName : String = ""
    var constructorArgs  = mutableListOf<ConstructorArgument>()

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    protected var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0

    lateinit var constructorBuilder : ConstructorBuilder


    fun initialize(builder : ConstructorBuilder){
        constructorBuilder = builder
        builder.getBlueprint<T>(this)
    }

    protected fun getSourceClass(): KClass<T>{
        return forClass!!
    }

    abstract fun getClass(): KClass<out T>

    fun setConstructor(constructor : KFunction<T>){
        effectiveConstructor = constructor
        effectiveConstructorSize = constructor.parameters.size
    }

    fun getArgsForConstructor(overrideDefault : ((name:String?)->Any?)? = null): Map<KParameter, Any?>{
        getConstructor().let { constructor ->
            val args = constructor.parameters.associateWith { param ->
                constructorBuilder.let { builder->
                    if(param.type.isMarkedNullable) {
                        null
                    }else{
                        val result = if(overrideDefault == null) {
                            builder.getDefaultForType(param.type)
                        }else{
                            overrideDefault.invoke(param.name)?:builder.getDefaultForType(param.type)
                        }
                        result
                    } }
            }
            this.setParams(args)
            return args
        }
    }

    open fun getConstructor(): KFunction<T>{
        return effectiveConstructor?: throw OperationsException("Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }

    fun addAsArg(param : KParameter){
        val paramName = param.name ?: "_"
        constructorArgs.add(ConstructorArgument(paramName, param.type, param.isOptional) )
    }

    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }
}