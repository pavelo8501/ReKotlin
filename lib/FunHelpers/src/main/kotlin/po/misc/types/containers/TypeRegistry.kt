package po.misc.types.containers

import po.misc.types.TypeData
import po.misc.types.Typed
import po.misc.types.containers.ThreeTypeRegistry
import po.misc.types.safeCast
import kotlin.reflect.KClass


sealed class TypeRegistry{
    abstract  val allTypes: List<Typed<*>>
    fun <T: Any> findType(kClass: KClass<T>): TypeData<T>?{
       val found =  allTypes.firstOrNull { it.kClass == kClass }
       return found?.safeCast<TypeData<T>>()
    }

    override fun equals(other: Any?): Boolean {
        return other is TypeRegistry && this.allTypes == other.allTypes
    }
    override fun hashCode(): Int  = allTypes.hashCode()
}

class TwoTypes<T1: Any, T2: Any>(
    val typeData1: Typed<T1>,
    val typeData2: Typed<T2>,
)

abstract class TwoTypeRegistry<T1: Any, T2: Any>(
   private val typeParameter1: Typed<T1>,
   private val typeParameter2: Typed<T2>
):TypeRegistry(){

    override val allTypes: List<Typed<*>> get() = listOf(typeParameter1, typeParameter2)

    internal constructor(twoTypes:TwoTypes<T1, T2>): this(twoTypes.typeData1, twoTypes.typeData2)

    companion object{
        inline fun <reified T1: Any, reified T2 : Any> create():TwoTypes<T1, T2>{
            return TwoTypes(TypeData.create<T1>(), TypeData.create<T2>())
        }
    }
}



class ThreeTypes<T1: Any, T2: Any, T3: Any>(
    val typeData1: TypeData<T1>,
    val typeData2: TypeData<T2>,
    val typeData3: TypeData<T3>,
)

abstract class ThreeTypeRegistry<T1: Any, T2: Any, T3: Any>(
   private val typeParameter1: TypeData<T1>,
   private val typeParameter2: TypeData<T2>,
   private val typeParameter3: TypeData<T3>
):TypeRegistry(){

   internal constructor(threeTypes:ThreeTypes<T1, T2, T3>): this(threeTypes.typeData1, threeTypes.typeData2, threeTypes.typeData3)

    override val allTypes: List<TypeData<*>> get() = listOf(typeParameter1, typeParameter2, typeParameter3)

    companion object{
        inline fun <reified T1: Any, reified T2 : Any, reified T3: Any> create():ThreeTypes<T1, T2, T3>{
           return ThreeTypes(TypeData.create<T1>(), TypeData.create<T2>(), TypeData.create<T3>())
        }
    }
}


