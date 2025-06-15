package po.test.misc.collections

import org.junit.jupiter.api.Test
import po.misc.collections.CompositeEnumKey
import po.misc.interfaces.Identifiable
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestCompositeKey {

    enum class SomeEnum{
        Value1,
        Value2
    }
    class SourceObject(override val sourceName: String) : Identifiable{
        override val componentName: String = "SomeName1"
    }

    class SameAsSourceObject(override val sourceName: String) : Identifiable{
        override val componentName: String = "SomeName1"
    }

    class SourceObject2(override val sourceName: String): Identifiable{
        override val componentName: String = "SomeName2"
    }

    val sourceObject : SourceObject = SourceObject("SomeName1Complete")
    val sameAsSourceObject : SameAsSourceObject = SameAsSourceObject("SomeName1Complete2")
    val sourceObject2 : SourceObject2 = SourceObject2("SomeName2Complete")

    @Test
    fun `composite keys  pass equality checks`(){
       val key1 = CompositeEnumKey(SomeEnum.Value1, sourceObject)
       val key2 = CompositeEnumKey(SomeEnum.Value1, sameAsSourceObject)
       val key3 = CompositeEnumKey(SomeEnum.Value1, sourceObject2)
       val key4 = CompositeEnumKey(SomeEnum.Value2, sameAsSourceObject)

        val keyedMap : Map<CompositeEnumKey<*>, String> = mapOf(key1 to "SomeString")

        assertNotEquals<CompositeEnumKey<*>>(key1, key3, "Keys are the same")
        assertEquals<CompositeEnumKey<*>>(key1, key2, "Keys are the same")
        assertNotEquals<CompositeEnumKey<*>>(key1, key4, "Keys with same object but different enum considered same")
        assertNotNull(keyedMap[key1])
        assertEquals("SomeString", keyedMap[key1])
        assertEquals(keyedMap[key1], keyedMap[key2])
        assertNull(keyedMap[key3])
    }

    @Test
    fun `helper created keys work the same way`(){

        val key1 =  CompositeEnumKey.generateKey(SomeEnum.Value1, sourceObject)
        val key2 =  CompositeEnumKey.generateKey(SomeEnum.Value1, sourceObject)
        assertEquals(key1, key2)
    }

}

