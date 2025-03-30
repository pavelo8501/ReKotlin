package po.exposify.constructors

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

    override fun getClass(): KClass<DATA> {
        return clazz
    }

    override fun addParameter(param: KParameter) {
        TODO("Not yet implemented")
    }

    override fun <N : Any> setNestedMap(map: Map<String, Map<String, ClassData<N>>>) {
        TODO("Not yet implemented")
    }

    override var  externalParamLookupFn : ( (type: KParameter) -> Any? )? = null
    @JvmName("externalParamLookupFnDataModelBlueprint")
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

    override fun getClass(): KClass<CommonDTO<DATA, ENTITY>> {
       return clazz
    }

}



abstract class ClassBlueprintBase<T: Any>(protected val clazz: KClass<T>) : BlueprintContainer<T> {

    var className: String = ""
    var qualifiedName : String = ""
    var constructorArgs  = mutableListOf<ConstructorArgument>()

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    var nestedClasses = mapOf<String,  Map<String, ClassData<*>>>()
    private set

    var propertyMap = mapOf<String, KProperty1<T, *>>()
    private set

    protected var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0

    lateinit var constructorBuilder : ConstructorBuilder


    override fun initialize(builder : ConstructorBuilder){
        constructorBuilder = builder
        builder.getBlueprint(clazz, this)
    }


    override fun setConstructor(constructor : KFunction<T>){
        effectiveConstructor = constructor
        effectiveConstructorSize = constructor.parameters.size
    }


    abstract var  externalParamLookupFn : ( (type: KParameter) -> Any? )?

    override fun getArgsForConstructor(): Map<KParameter, Any?>{
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

    override fun getConstructor(): KFunction<T>{
        return effectiveConstructor?:
            throw OperationsException("Effective constructor not set", ExceptionCodes.CONSTRUCTOR_MISSING)
    }

    override fun addParameter(param : KParameter){
        val paramName = param.name ?: "_"
        constructorArgs.add(ConstructorArgument(paramName, param.type, param.isOptional) )
    }

    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }

    override fun <N : Any> setNestedMap(map : Map<String, Map<String, ClassData<N>>>){
        nestedClasses = map
    }

    override fun setPropertyMap(map:Map<String, KProperty1<T, *>>){
        propertyMap = map
    }
}