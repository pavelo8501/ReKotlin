package po.misc.reflection.classes

import po.misc.types.isNull
import kotlin.Boolean
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.typeOf

enum class ClassRole{
    Receiver,
    Result,
    MethodParameter,
    Delegate
}

data class TypeTraits(
    val isUnit: Boolean,
    val isEnum: Boolean,
    val isNullable: Boolean,
    val isFunction: Boolean,
    val isPrimitive: Boolean,
    val isObject: Boolean,
    val isDataClass: Boolean,
    val isSealed: Boolean,
    val isInterface: Boolean,
    val isAbstract: Boolean
)

data class ClassInfo<T: Any?>(
    val classRole :ClassRole,
    val simpleName: String,
    val traits : TypeTraits,
) {
    val implementationMap = mutableMapOf<String, Boolean>()

    val acceptsNull: Boolean get(){
        return classRole == ClassRole.Result && (traits.isUnit || traits.isNullable)
    }



    fun <I: Any> implements(receiver:I, superClass: KClass<*>) {
        val result = receiver::class.isSubclassOf(superClass)
        implementationMap[superClass.simpleName.toString()] = result
    }
}

fun <T: Any?> overallInfoFromType(
    role: ClassRole,
    type: KType
): ClassInfo<T> {
    val kClass = type.classifier as? KClass<*> ?: throw IllegalStateException("Unsupported type classifier")
    return ClassInfo(
        classRole = role,
        simpleName = kClass.simpleName ?: "<anonymous>",
        traits =  analyzeType(type)
    )
}

inline fun <reified R: Any?> overallInfo(role: ClassRole): ClassInfo<R> {
    val traits = analyzeType<R>()
    return overallInfoFromType<R>(role, typeOf<R>())
}


inline fun <T: Any> T.overallInfo(
    role:ClassRole,
    block: ClassInfo<T>.(T)-> Unit
):ClassInfo<T>{
   val  clazz : KClass<out T> = this::class
    val isFunction : Boolean =  clazz.isFun
    val simpleName: String = clazz.simpleName.toString()
    val isNullable: Boolean = clazz::class.isNull()
    val info = ClassInfo<T>(
        classRole = role,
        simpleName = simpleName,
        traits = analyzeInstanceType(this),
    )
    block.invoke(info, this)
   return info
}

inline fun <reified T> analyzeType(): TypeTraits {
    val type = typeOf<T>()
    val kClass = type.classifier as? KClass<*> ?: throw IllegalArgumentException("Unknown classifier")

    return TypeTraits(
        isUnit = kClass == Unit::class,
        isEnum = kClass.isSubclassOf(Enum::class),
        isNullable = type.isMarkedNullable,
        isFunction = kClass.isSubclassOf(Function::class),
        isPrimitive = kClass.java.isPrimitive,
        isObject = kClass.objectInstance != null,
        isDataClass = kClass.isData,
        isSealed = kClass.isSealed,
        isInterface = kClass.java.isInterface,
        isAbstract = kClass.isAbstract
    )
}


fun analyzeType(type: KType): TypeTraits {
    val kClass = type.classifier as? KClass<*> ?: throw IllegalArgumentException("Unknown classifier")
    return TypeTraits(
        isUnit = kClass == Unit::class,
        isEnum = kClass.isSubclassOf(Enum::class),
        isNullable = type.isMarkedNullable,
        isFunction = kClass.isSubclassOf(Function::class),
        isPrimitive = kClass.java.isPrimitive,
        isObject = kClass.objectInstance != null,
        isDataClass = kClass.isData,
        isSealed = kClass.isSealed,
        isInterface = kClass.java.isInterface,
        isAbstract = kClass.isAbstract
    )
}

fun <T : Any> analyzeInstanceType(instance: T): TypeTraits {
    val kClass = instance::class
    return TypeTraits(
        isUnit = kClass == Unit::class,
        isEnum = kClass.isSubclassOf(Enum::class),
        isNullable = false, // cannot know at runtime
        isFunction = kClass.isSubclassOf(Function::class),
        isPrimitive = kClass.java.isPrimitive,
        isObject = kClass.objectInstance != null,
        isDataClass = kClass.isData,
        isSealed = kClass.isSealed,
        isInterface = kClass.java.isInterface,
        isAbstract = kClass.isAbstract
    )
}