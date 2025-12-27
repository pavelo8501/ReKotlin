package po.misc.types.token

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.ClassInfo
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.typeOf


data class GenericInfo(
    val parameterName: String,
    val kType: KType,
    val classInfo: ClassInfo
): PrettyPrint {
    private val classDisplayName: String  get() {
        return if(kType.isMarkedNullable){
            "${classInfo.simpleName}?".colorize(Colour.Yellow)
        }else{
            classInfo.simpleName.colorize(Colour.Yellow)
        }
    }
    val isMarkedNullable: Boolean get() = kType.isMarkedNullable
    override val formattedString: String = "${parameterName.colorize(Colour.GreenBright)}: $classDisplayName"
}


enum class TypeLitera {
    T,V,T2,E,R,UNKNOWN, Class
}

class TypeSlot(
    val parameter: KTypeParameter,
): PrettyPrint{


    constructor(parameter: KTypeParameter, typeToken: TypeToken<*>):this(parameter){
        token = typeToken
    }

    var token: TypeToken<*>? = null
    private set

    val kClass: KClass<*>? get() =  token?.kClass
    val type: KType? get() =  token?.kType

    val classResolved: Boolean get() = kClass != null

    val parameterName: String = parameter.name
    val upperBoundsClasses: List<KClass<*>> get()
    {
        val result = parameter.upperBounds.mapNotNull { it.classifier as? KClass<*> }
        if(result.isEmpty()) return listOf(Any::class)
        return result
    }

    val upperBounds : KClass<*> get() = upperBoundsClasses.first()
    val parameterClass : KClass<*> get() = kClass?:upperBounds
    val typeLitera : TypeLitera get() {
       return  TypeLitera.entries.toList().firstOrNull { it.name == parameterName }?:TypeLitera.UNKNOWN
    }

    val genericParamName: String get() =  "$parameterName: ${parameterClass.simpleOrAnon}"
    
    override val formattedString: String get() = buildString{
        append(parameterName)
        append(": ")
        append(parameterClass.simpleOrAnon)
    }

    fun resolve(typeToken: TypeToken<*>){
        token = typeToken
    }

    fun toComparePair():Pair<KClass<*>, Boolean>?{
        val slotToken = token
        if(slotToken != null){
            return Pair(slotToken.kClass, slotToken.isNullable)
        }
       return null
    }
    override fun toString(): String {
        return genericParamName
    }
}
