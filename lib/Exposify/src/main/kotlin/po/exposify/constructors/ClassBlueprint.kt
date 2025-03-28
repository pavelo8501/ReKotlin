package po.exposify.constructors

import com.mysql.cj.x.protobuf.MysqlxCrud
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.dto.CommonDTO
import kotlin.collections.mapOf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
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
    clazz : KClass<DATA>,
    constructor: ConstructorBuilder? = null
) : ClassBlueprintBase<DATA>(clazz){

    init {
        if(constructor!=null){
            initialize(constructor)
        }
    }

    fun getClass(): KClass<DATA> {
        return clazz
    }

    override var  externalParamLookupFn : ( (type: KParameter) -> Any? )? = null
    @JvmName("externalParamLookupFnDataModelBlueprint")
    fun setExternalParamLookupFn(fn : (type: KParameter) -> Any? ){
        externalParamLookupFn = fn
    }

}


class EntityBlueprint<ENTITY : LongEntity >(
    clazz : KClass<ENTITY>
) : ClassBlueprintBase<ENTITY>(clazz){

    fun getClass(): KClass<ENTITY> {
       return clazz
    }
    override var  externalParamLookupFn : ( (type: KParameter) -> Any? )? = null
    @JvmName("externalParamLookupFnEntityBlueprint")
    fun setExternalParamLookupFn(fn : (type: KParameter) -> Any? ){
        externalParamLookupFn = fn
    }

}

class DTOBlueprint<DATA, ENTITY>(
    clazz  : KClass<out CommonDTO<DATA, ENTITY>>,
) : ClassBlueprintBase<CommonDTO<DATA,ENTITY>>(
    clazz as KClass<CommonDTO<DATA, ENTITY>>)
        where ENTITY : LongEntity, DATA : DataModel{


    override var  externalParamLookupFn : ( (type: KParameter) -> Any? )? = null
    @JvmName("externalParamLookupFnDTOBlueprint")
    fun setExternalParamLookupFn(fn : (type: KParameter) -> Any? ){
        externalParamLookupFn = fn
    }

}





//abstract class CovariantClassBlueprintBase<T: Any>(val clazz: KClass<out T>){
//
//    protected var effectiveConstructor : KFunction<T>? = null
//    private var effectiveConstructorSize : Number  = 0
//
//    lateinit var constructorBuilder : ConstructorBuilder
//
//    fun getConstructor(): KFunction<T>{
//        return effectiveConstructor?: throw OperationsException(
//            "Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
//    }
//
//    fun initialize(builder : ConstructorBuilder){
//        constructorBuilder = builder
//        builder.getBlueprint(getClass(), this)
//    }
//
//    fun getClass():KClass<out T>{
//        return clazz
//    }
//}



abstract class ClassBlueprintBase<T: Any>(protected val clazz: KClass<T>){

    var className: String = ""
    var qualifiedName : String = ""
    var constructorArgs  = mutableListOf<ConstructorArgument>()

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    var nestedClasses = mapOf<String,  Map<String,ClassData>>()
    private set

    var propertyMap = mapOf<String, KProperty1<T, *>>()
    private set

    protected var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0

    lateinit var constructorBuilder : ConstructorBuilder


    fun initialize(builder : ConstructorBuilder){
        constructorBuilder = builder
        builder.getBlueprint(clazz, this)
    }


    fun setConstructor(constructor : KFunction<T>){
        effectiveConstructor = constructor
        effectiveConstructorSize = constructor.parameters.size
    }


    abstract var  externalParamLookupFn : ( (type: KParameter) -> Any? )?
    @JvmName("externalParamLookupFnOfBaseClass")
    fun getArgsForConstructor(): Map<KParameter, Any?>{
        getConstructor().let { constructor ->
            val args =  constructor.parameters.associateWith { param ->
                constructorBuilder.let { builder->
                    if(param.type.isMarkedNullable) {
                        null
                    }else {
                        builder.getDefaultForType(param.type) ?: run {
                             externalParamLookupFn?.invoke(param)

                        }
                    } }
            }
            this.setParams(args)
            return args
        }
    }

    open fun getConstructor(): KFunction<T>{
        return effectiveConstructor?:
            throw OperationsException("Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }

    fun addAsArg(param : KParameter){
        val paramName = param.name ?: "_"
        constructorArgs.add(ConstructorArgument(paramName, param.type, param.isOptional) )
    }

    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }

    fun setNestedMap(map : Map<String, Map<String,ClassData>>){
        nestedClasses = map
    }

    fun setPropertyMap(map:Map<String, KProperty1<T, *>>){
        propertyMap = map
    }
}