package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.types.helpers.filterByType
import po.misc.types.helpers.filterByTypeWhere
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestTypeToken: TypeTestBase() {

    @Test
    fun `If type data comparison work as expected2`(){

        val doubleTypeExtraParam = TypeToken.create<TypeHolder2<ComponentInt, SealedInheritor>>()

        val clazz2 =  doubleTypeExtraParam.typeSlots.filter {typeSlot->
            typeSlot.ownClass != null && typeSlot.ownReifiedKType != null
        }.firstNotNullOfOrNull{ it.ownClass }

        doubleTypeExtraParam.typeName.output(Colour.CyanBright)

        assertNotNull(clazz2)

    }


    @Test
    fun `If type data comparison work as expected`(){

        val doubleTypeDate = TypeToken.create<TypeHolder2<ComponentInterface, SealedInheritor>>()
        assertEquals(2, doubleTypeDate.typeSlots.size)

        val doubleTypeExtraParamByTypeData = TypeToken.create<TypeHolder2<ComponentInt, SealedInheritor>>()
        assertEquals(2, doubleTypeExtraParamByTypeData.typeSlots.size)
        val clazz = doubleTypeExtraParamByTypeData.typeSlots.filter {typeSlot->
            typeSlot.ownClass != null && typeSlot.ownReifiedKType != null
        }.firstNotNullOfOrNull{ it.ownClass }

        val inheritorClass = assertNotNull(clazz)
        assertEquals(SealedInheritor::class, inheritorClass)
        doubleTypeExtraParamByTypeData.typeSlots.output()

        val doubleTypeExtraParam = TypeToken.create<TypeHolder2<ComponentInt, SealedInheritor>>()

        val clazz2 =  doubleTypeExtraParam.typeSlots.filter {typeSlot->
            typeSlot.ownClass != null && typeSlot.ownReifiedKType != null
        }.firstNotNullOfOrNull{ it.ownClass }

        val componentIntClass = assertNotNull(clazz2)
        assertEquals(ComponentInt::class, componentIntClass)

        doubleTypeExtraParam.typeSlots.output()
    }

    @Test
    fun `Filtering by new type data`(){

        val listOfHolders = mutableListOf<TypeHolder2<*, *>>()

        val holdersByInt =  createHoldersComponentInt(1)
        listOfHolders.addAll(holdersByInt)
        assertEquals(1, holdersByInt.size)

        val holdersByString =   createHoldersComponentStr(2)
        listOfHolders.addAll(holdersByString)
        assertEquals(2, holdersByString.size)

        val filtrationResult =  listOfHolders.filterByType<TypeHolder2<ComponentInt, SealedInheritor>>()
        assertEquals(1, filtrationResult.size)
    }

    @Test
    fun `Filtering by new type data and predicate`() {

        val listOfHolders = mutableListOf<TypeHolder2<*, *>>()
        val holdersByInt = createHoldersComponentInt(4)
        listOfHolders.addAll(holdersByInt)
        assertEquals(4, holdersByInt.size)

        val holdersByString = createHoldersComponentStr(5)
        listOfHolders.addAll(holdersByString)
        assertEquals(5, holdersByString.size)

        val result =  listOfHolders.filterByTypeWhere<TypeHolder2<ComponentInt, SealedInheritor>>{filterBy->
            filterBy.param1Instance.value <= 2
        }
        assertEquals(2, result.size)
    }

    @Test
    fun `No generic params available filtration`(){

        val listOfHolders = mutableListOf<TypeHolder2<ComponentInt, out SealedBase>>()
        val holdersByInt =  createHoldersComponentInt(4)

        listOfHolders.addAll(holdersByInt)
        assertEquals(4, holdersByInt.size)

        val result = listOfHolders.filterByType(TypeToken.create<TypeHolder2<ComponentInt, SealedInheritor>>())
        assertEquals(4, result.size)
    }

}