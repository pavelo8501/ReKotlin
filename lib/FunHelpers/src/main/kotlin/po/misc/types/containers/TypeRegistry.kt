package po.misc.types.containers

import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


sealed class TypeRegistry{
    abstract  val allTypes: List<TypeToken<*>>
    fun <T: Any> findType(kClass: KClass<T>): TypeToken<T>?{
       val found =  allTypes.firstOrNull { it.kClass == kClass }
       return found?.safeCast<TypeToken<T>>()
    }

    override fun equals(other: Any?): Boolean {
        return other is TypeRegistry && this.allTypes == other.allTypes
    }
    override fun hashCode(): Int  = allTypes.hashCode()
}

class TwoTypes<T1: Any, T2: Any>(
    val typeData1: TypeToken<T1>,
    val typeData2: TypeToken<T2>,
)

abstract class TwoTypeRegistry<T1: Any, T2: Any>(
   private val typeParameter1: TypeToken<T1>,
   private val typeParameter2: TypeToken<T2>
):TypeRegistry(){

    override val allTypes: List<TypeToken<*>> get() = listOf(typeParameter1, typeParameter2)

    internal constructor(twoTypes:TwoTypes<T1, T2>): this(twoTypes.typeData1, twoTypes.typeData2)

    companion object{
        inline fun <reified T1: Any, reified T2 : Any> create():TwoTypes<T1, T2>{
            return TwoTypes(TypeToken.create<T1>(), TypeToken.create<T2>())
        }
    }
}

class ThreeTypes<T1: Any, T2: Any, T3: Any>(
    val typeData1: TypeToken<T1>,
    val typeData2: TypeToken<T2>,
    val typeData3: TypeToken<T3>,
)

abstract class ThreeTypeRegistry<T1: Any, T2: Any, T3: Any>(
   private val typeParameter1: TypeToken<T1>,
   private val typeParameter2: TypeToken<T2>,
   private val typeParameter3: TypeToken<T3>
):TypeRegistry(){

   internal constructor(threeTypes:ThreeTypes<T1, T2, T3>): this(threeTypes.typeData1, threeTypes.typeData2, threeTypes.typeData3)

    override val allTypes: List<TypeToken<*>> get() = listOf(typeParameter1, typeParameter2, typeParameter3)

    companion object{
        inline fun <reified T1: Any, reified T2 : Any, reified T3: Any> create():ThreeTypes<T1, T2, T3>{
           return ThreeTypes(TypeToken.create<T1>(), TypeToken.create<T2>(), TypeToken.create<T3>())
        }
    }
}


