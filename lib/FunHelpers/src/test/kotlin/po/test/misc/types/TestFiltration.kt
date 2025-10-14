package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.types.helpers.filterByType
import po.misc.types.helpers.filterByTypeWhere
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestFiltration: TypeTestBase() {

    @Test
    fun `General type list filtration work as expected`(){

        val intComponents =  createIntComponents(3)
        val filtration1 =  intComponents.filterByType<ComponentInt>()
        assertEquals(intComponents.size, filtration1.size)

        val filtration2 =  intComponents.filterByType<ComponentInt>(ComponentString::class)
        assertTrue { filtration2.isEmpty() }
    }

    @Test
    fun `List with object implementing Tokenized interface filtration`(){

        val tokenizedList =  createHoldersComponentInt(3)
        val filtration1 =  tokenizedList.filterByType<TypeHolder2<ComponentInterface, SealedBase>>()
        assertEquals(tokenizedList.size, filtration1.size)

        val filtration2 =  tokenizedList.filterByType<TypeHolder2<ComponentInt, SealedBase>>()
        assertEquals(tokenizedList.size, filtration2.size)

        val filtration3 =  tokenizedList.filterByType<TypeHolder2<ComponentInt, SealedInheritor>>()
        assertEquals(tokenizedList.size, filtration3.size)
    }

    @Test
    fun `Tokenized list filtration filters out incorrect types`(){
        val tokenizedList =  createHoldersComponentInt(3)
        val filtration1 =  tokenizedList.filterByType<TypeHolder2<ComponentString, SealedInheritor>>(ComponentString::class)
        assertTrue { filtration1.isEmpty() }
    }

    @Test
    fun `Tokenized list filtration with predicate`(){
        val tokenizedList =  createHoldersComponentInt(3)

        val filtration1 =  tokenizedList.filterByTypeWhere<TypeHolder2<ComponentInt, SealedInheritor>>(ComponentInt::class){
            it.param1Instance.value == 1
        }
        assertEquals(1, filtration1.size)
        assertEquals(1,  filtration1.first().param1Instance.value)
    }

}