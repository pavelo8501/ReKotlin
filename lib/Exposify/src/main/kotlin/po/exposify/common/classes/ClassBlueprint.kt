package po.exposify.common.classes

import po.exposify.common.interfaces.BlueprintContainer
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.types.getOrManaged
import kotlin.collections.mapOf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties


enum class ConstructorParams{
    UsePrimary,
    UseSecondary
}

data class ConstructorArgument(
    val name:String,
    val type: KType,
    val nullable : Boolean
)

class ClassBlueprint<T>(
    val clazz  : KClass<T>,
    val params : ConstructorParams = ConstructorParams.UsePrimary,
    val builder : ConstructorBuilder = ConstructorBuilder(),
): BlueprintContainer<T> where T: Any {
    var  paramLookupFn : ( (type: KParameter) -> Any? )? = null

    var className: String = ""
    private var constructorArgs: MutableList<ConstructorArgument> = mutableListOf<ConstructorArgument>()
    var nestedClasses: Map<String, Map<String, ClassData<*>>> = mapOf<String,  Map<String, ClassData<*>>>()
        private set

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    protected var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0
    private val properties: MutableMap<String, KProperty1<T, *>> = mutableMapOf()

    init {
        initialize()
    }

    fun setExternalParamLookupFn(fn : (type: KParameter) -> Any? ){
        paramLookupFn = fn
    }
    override fun getConstructor(): KFunction<T> {
        return effectiveConstructor?:
        throw OperationsException("Effective constructor not set", ExceptionCode.CONSTRUCTOR_MISSING, null)
    }
    override fun initialize(){
        className = clazz.simpleName.toString()
        builder.getBlueprint(clazz, this, params)
        clazz.memberProperties.forEach {
            properties[it.name] = it
        }
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
    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }
    override fun <N:Any>setNestedMap(map: Map<String, Map<String, ClassData<N>>>) {
        nestedClasses = map
    }

   fun getValue(instance:T, name: String): Any?{
        val property = properties[name].getOrManaged("Property with name: $name not found in ${clazz.simpleName}")
        return property.get(instance)
   }

    fun getValueCheckType(instance:T, name: String): Any?{
        val property = properties[name].getOrManaged("Property with name: $name not found in ${clazz.simpleName}")
        return property.get(instance)
    }

    override fun getConstructorArgs(): Map<KParameter, Any?> {
        getConstructor().let { constructor ->
            val args =  constructor.parameters.associateWith { param ->
                builder.let { builder->
                    if(param.type.isMarkedNullable) {
                        null
                    }else {
                        builder.getDefaultForType(param.type) ?: run {
                            paramLookupFn?.invoke(param)

                        }
                    } }
            }
            setParams(args)
            return args
        }
    }
}

