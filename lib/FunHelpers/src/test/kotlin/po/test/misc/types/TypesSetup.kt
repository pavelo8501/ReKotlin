package po.test.misc.types

import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken

interface ComponentInterface

class ComponentString(val value: String = "Str value"):ComponentInterface

class ComponentInt(val value: Int = 10):ComponentInterface


sealed class SealedBase(val message: String)

class SealedInheritor(message: String): SealedBase(message){

    companion object: TokenHolder{
        override val typeToken: TypeToken<SealedInheritor> = TypeToken.create<SealedInheritor>()
        val newTypeData: TypeToken<SealedInheritor> = TypeToken.create<SealedInheritor>()
    }
}

class TypeHolder2<C: ComponentInterface, U: SealedBase>(
   override val typeToken: TypeToken<TypeHolder2<C, U>>,
   val param1Instance: C,
   val param2Instance: U,
): TokenHolder {
    companion object
}

class TypeHolder<C: ComponentInterface, U: SealedBase>(
    override val typeToken: TypeToken<TypeHolder<C, U>>
):TokenHolder {

    companion object{
        inline operator fun <reified C: ComponentInterface, reified U: SealedBase> invoke():TypeHolder<C, U>{
            val data = TypeToken.create<TypeHolder<C, U>>()
           return TypeHolder(data)
        }
    }
}

