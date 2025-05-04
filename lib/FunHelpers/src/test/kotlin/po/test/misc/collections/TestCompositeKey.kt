package po.test.misc.collections

import org.junit.jupiter.api.Test
import po.misc.collections.CompositeKey
import po.misc.collections.Identifiable
import po.misc.collections.generateKey
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestCompositeKey {

    enum class SomeEnum{
        Value1,
        Value2
    }
    class SourceObject() : Identifiable{
        override val qualifiedName: String = "SomeName1"
    }

    class SameAsSourceObject() : Identifiable{
        override val qualifiedName: String = "SomeName1"
    }

    class SourceObject2(): Identifiable{
        override val qualifiedName: String = "SomeName2"
    }

    val sourceObject : SourceObject = SourceObject()
    val sameAsSourceObject : SameAsSourceObject = SameAsSourceObject()
    val sourceObject2 : SourceObject2 = SourceObject2()

    @Test
    fun `composite keys  pass equality checks`(){
       val key1 = CompositeKey(sourceObject, SomeEnum.Value1)
       val key2 = CompositeKey(sameAsSourceObject, SomeEnum.Value1)
       val key3 = CompositeKey(sourceObject2, SomeEnum.Value1)
       val key4 = CompositeKey(sameAsSourceObject, SomeEnum.Value2)

        val keyedMap : Map<CompositeKey<*,*>, String> = mapOf(key1 to "SomeString")

        assertNotEquals<CompositeKey<*,*>>(key1, key3, "Keys are the same")
        assertEquals<CompositeKey<*,*>>(key1, key2, "Keys are the same")
        assertNotEquals<CompositeKey<*,*>>(key1, key4, "Keys with same object but different enum considered same")
        assertNotNull(keyedMap[key1])
        assertEquals("SomeString", keyedMap[key1])
        assertEquals(keyedMap[key1], keyedMap[key2])
        assertNull(keyedMap[key3])
    }

    @Test
    fun `helper created keys work the same way`(){

        val key1 = sourceObject.generateKey(SomeEnum.Value1)
        val key2 = sourceObject.generateKey(SomeEnum.Value1)
        assertEquals(key1, key2)
    }

}

