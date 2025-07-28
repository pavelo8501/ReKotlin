package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.types.TypeData
import po.misc.types.containers.ThreeTypeRegistry
import po.test.misc.reflection.TestPropertyHelpers
import po.test.misc.types.TestTypeData.SourceClass1
import po.test.misc.types.TestTypeData.SourceClass2
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTypeRegistry {

    internal class ThreeGenericsClassType(): ThreeTypeRegistry<SourceClass1, SourceClass2, TestPropertyHelpers.SourceClass>(create())

    internal class OtherThreeGenericsClassType(
        val sourceClass1Type: TypeData<SourceClass1> = TypeData.create<SourceClass1>(),
        val sourceClass2Type: TypeData<SourceClass2> = TypeData.create<SourceClass2>(),
        val sourceClassType: TypeData<TestPropertyHelpers.SourceClass> = TypeData.create<TestPropertyHelpers.SourceClass>()
    ) : ThreeTypeRegistry<SourceClass1, SourceClass2, TestPropertyHelpers.SourceClass>(sourceClass1Type, sourceClass2Type, sourceClassType)

    @Test
    fun `Type registry comparison is correct`() {
        val registry1 = ThreeGenericsClassType()
        val registry2 = OtherThreeGenericsClassType()
        val result = registry1 == registry2

        assertTrue(result)
    }

    @Test
    fun `Type registry usage`() {
        val registry = ThreeGenericsClassType()
        assertEquals(3, registry.allTypes.size)
        assertNotNull(registry.findType(SourceClass1::class))
    }
}