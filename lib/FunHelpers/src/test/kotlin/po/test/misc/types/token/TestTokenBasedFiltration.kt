package po.test.misc.types.token

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.types.helpers.filterByType
import po.misc.types.token.TokenFactory
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.types.ComponentInt
import po.test.misc.types.SealedInheritor
import po.test.misc.types.TypeHolder2
import po.test.misc.types.TypeTestBase
import kotlin.test.assertEquals


class TestTokenBasedFiltration() : TypeTestBase(), TokenFactory{

    private class GenericParam<T: Any, V: Any>(
        val genericParam:T,
        val genericValue:V,
        val parameter: String = "Parameter String",
    ): TraceableContext, Tokenized<GenericParam<T, V>>{
        override val typeToken: TypeToken<GenericParam<T, V>> = TypeToken.create()
    }


    @Test
    fun `Filtering by new type data`(){

        val listOfHolders = mutableListOf<TypeHolder2<*, *>>()

        val holdersByInt = createHoldersComponentInt(1)
        listOfHolders.addAll(holdersByInt)
        assertEquals(1, holdersByInt.size)

        val holdersByString = createHoldersComponentStr(2)
        listOfHolders.addAll(holdersByString)
        assertEquals(2, holdersByString.size)

        val filtrationResult =  listOfHolders.filterByType<TypeHolder2<ComponentInt, SealedInheritor>>(ComponentInt::class)
        assertEquals(1, filtrationResult.size)

        val token = tokenOf<TypeHolder2<ComponentInt, SealedInheritor>>()
        val filtrationWithToken =  listOfHolders.filterByType<TypeHolder2<ComponentInt, SealedInheritor>>(token)
        assertEquals(1, filtrationWithToken.size)
    }


    @Test
    fun `Filtering by another token `(){




    }


}