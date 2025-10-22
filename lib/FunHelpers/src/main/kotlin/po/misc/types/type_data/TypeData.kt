package po.misc.types.type_data

import po.misc.collections.ComparableType
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.Verbosity
import po.misc.types.token.TypeToken

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


interface TypeDataCommon<T: Any>{
    val kClass: KClass<T>
    val kType: KType
}

@Deprecated("Switch to TypeToken")
class TypeData<T: Any> @PublishedApi internal constructor (
    override val kClass: KClass<T>,
    override val kType: KType
): ComparableType<T>, TypeDataCommon<T>, Component {

    val simpleName : String get() = kClass.simpleName?:"Unknown"
    val qualifiedName: String get() = kClass.qualifiedName?:"Unknown"
    val typeData : TypeParameterData =  kType.toTypeParameterData()
    val tParamKClasses: List<KClass<*>> = typeData.children.mapNotNull { it.kClass }
    val children: List<TypeData<*>> =  listOf(this as TypeData<*>)


    override val componentID: ComponentID = componentID("TypeData")
    val verbosity: Verbosity = Verbosity.Info


    override val typeName: String = simpleName + children.joinToString(prefix = "<", separator = " ,", postfix = ">") {
        it.typeName
    }

    override fun hashCode(): Int = kType.hashCode()

    override fun equals(other: Any?): Boolean {
        return other is TypeToken<*> &&
                this.kType == other.kType
    }

    override fun toString(): String = "TypeData<$typeName>"

    companion object{
        inline operator fun <reified T: Any> invoke(targetClas : KClass<out T>):TypeToken<T>{
           val data = TypeToken(T::class, typeOf<T>())
           return data
        }
        inline fun <reified T: Any> create():TypeToken<T>{
            return TypeToken(T::class, typeOf<T>())
        }
    }
}

