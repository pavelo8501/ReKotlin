package po.exposify.common.classes


import po.exposify.common.interfaces.BlueprintContainer
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import kotlin.collections.mapOf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


data class ConstructorArgument(
    val name:String,
    val type: KType,
    val nullable : Boolean
)


class ClassBlueprint<T>(
   val clazz  : KClass<T>
): BlueprintContainer<T> where T: Any {
    var  paramLookupFn : ( (type: KParameter) -> Any? )? = null

    var className: String = ""

    fun setExternalParamLookupFn(fn : (type: KParameter) -> Any? ){
        paramLookupFn = fn
    }

    private var constructorArgs: MutableList<ConstructorArgument> = mutableListOf<ConstructorArgument>()

    var nestedClasses: Map<String, Map<String, ClassData<*>>> = mapOf<String,  Map<String, ClassData<*>>>()
        private set

    var constructorParams  = mutableMapOf<KParameter,  Any?>()
        private set

    protected var effectiveConstructor : KFunction<T>? = null
    private var effectiveConstructorSize : Number  = 0

    lateinit var constructorBuilder : ConstructorBuilder

    override fun getConstructor(): KFunction<T> {
        return effectiveConstructor?:
        throw OperationsException("Effective constructor not set", ExceptionCode.CONSTRUCTOR_MISSING)
    }

    override fun initialize(builder : ConstructorBuilder){
        className = clazz.simpleName.toString()
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

    fun setParams(params :  Map<KParameter,  Any?>){
        constructorParams = params.toMutableMap()
    }

    override fun <N:Any>setNestedMap(map: Map<String, Map<String, ClassData<N>>>) {
        nestedClasses = map
    }

   inline fun <reified V : Any> getProperty(instance:T, name: String):V{
        return clazz.memberProperties.firstOrNull{it.name == name}?.get(instance).castOrOperationsEx<V>()
    }

    fun <V> setProperty(instance:T, name: String, value:V){
        val property =  clazz.memberProperties.firstOrNull{it.name == name}.getOrOperationsEx()
        property.isAccessible = true
        val casted = property.castOrOperationsEx<KMutableProperty1<T,V>>()
        casted.set(instance, value)
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
            }
            setParams(args)
            return args
        }
    }
}

