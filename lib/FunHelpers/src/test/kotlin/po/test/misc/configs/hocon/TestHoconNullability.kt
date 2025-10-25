package po.test.misc.configs.hocon

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.createResolver
import po.misc.configs.hocon.extensions.applyConfig
import po.misc.configs.hocon.properties.hoconNested
import po.misc.configs.hocon.properties.hoconProperty
import po.misc.configs.hocon.properties.listProperty
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestHoconNullability {

    private class NullableConfig: HoconResolvable<NullableConfig> {
        override val resolver = createResolver()

        val number : Int? by hoconProperty()
        val string: String by hoconProperty()
    }
    private class ListConfig: HoconResolvable<ListConfig> {
        override val resolver = createResolver()
        val records : List<String>? by listProperty()
    }

    private class Nested: HoconResolvable<Nested> {
        override val resolver = createResolver()
        val nestedProperty : String by hoconProperty()
    }

    private class NestedHolder: HoconResolvable<NestedHolder> {
        override val resolver = createResolver()

        val nested : Nested? by hoconNested(Nested())
        val string: String by hoconProperty()
    }

    @Test
    fun `Entry and property correctly resolved to nullable type`(){
        val nullableConfig = NullableConfig()
        val entry = nullableConfig.resolver.entryMap["number"]
        val intEntry =  assertNotNull(entry)
        assertTrue { intEntry.nullable }

        val stringEntry =  assertNotNull(nullableConfig.resolver.entryMap["string"])
        assertFalse { stringEntry.nullable }
    }

    @Test
    fun `Parsing data from hocon both fields present`() {
        val factory = ConfigFactory.load().getConfig("test")
        val nullableConfig = NullableConfig()

        val parsed =  assertDoesNotThrow { nullableConfig.applyConfig(factory) }
        assertEquals(300, parsed.number)
        assertEquals("some string", parsed.string)
    }

    @Test
    fun `Parsing data from hocon,  number field is null`() {
        val factory = ConfigFactory.load().getConfig("test_null")
        val nullableConfig = NullableConfig()
        val parsed =  assertDoesNotThrow { nullableConfig.applyConfig(factory) }
        assertEquals(null, parsed.number)
        assertEquals("some string", parsed.string)
    }

    @Test
    fun `Parsing list of strings`() {
        val factory = ConfigFactory.load().getConfig("test_records")
        val listConfig = ListConfig()
        val entry = listConfig.resolver.entryMap["records"]
        assertNotNull(entry)
        val parsed =  assertDoesNotThrow { listConfig.applyConfig(factory) }
        val records = assertNotNull(parsed.records)
        assertEquals(3, records.size)
    }

    @Test
    fun `Parsing hocon, records field is null`() {
        val factory = ConfigFactory.load().getConfig("test_null_records")
        val listConfig = ListConfig()
        val entry = listConfig.resolver.entryMap["records"]
        assertNotNull(entry)
        val parsed =  assertDoesNotThrow { listConfig.applyConfig(factory) }
        val recordsParsed =  assertDoesNotThrow {
            parsed.records
        }
        assertNotNull(recordsParsed)
        assertTrue {
            recordsParsed.isEmpty()
        }
    }

    @Test
    fun `Parsing hocon, with nested config`() {
        val factory = ConfigFactory.load().getConfig("test_nested")
        val nestedHolder = NestedHolder()
        val member = nestedHolder.resolver.memberMap[Nested::class]
        val nested =  assertNotNull(member)

        val nestedEntry = nested.resolver.entryMap["nestedproperty"]
        assertNotNull(nestedEntry)

        val entry = nestedHolder.resolver.entryMap["string"]
        assertNotNull(entry)
        val recordsParsed =  assertDoesNotThrow{ nestedHolder.applyConfig(factory) }
        assertEquals("some string", recordsParsed.string)
        val nestedConfig = assertNotNull(recordsParsed.nested)
        assertEquals("some nested string", nestedConfig.nestedProperty)
    }

    @Test
    fun `Parsing hocon, with nullable nested config`(){

        val factory = ConfigFactory.load().getConfig("test_nested_nullable")
        val nestedHolder = NestedHolder()
        val member = nestedHolder.resolver.memberMap[Nested::class]
        val nested =  assertNotNull(member)
        val nestedEntry = assertNotNull(nestedHolder.resolver.entryMap["nested"])
        assertTrue {
            nestedEntry.nullable
        }
        nestedHolder.applyConfig(factory)

    }

}