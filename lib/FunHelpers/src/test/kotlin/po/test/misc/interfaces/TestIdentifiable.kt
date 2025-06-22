package po.test.misc.interfaces

import org.junit.jupiter.api.Test
import po.misc.interfaces.Named


class TestIdentifiable {

    enum class Module (override val moduleName: String): Named {
        TestIdent("TestIdentifiable");

        override fun toString(): String {
            return moduleName
        }
    }

    @Test
    fun `Test usage`(){

//        val identifiable: IdentifiableImplementation = asIdentifiable("SourceName", "ComponentName")
//        val module: IdentifiableModule = asIdentifiableModule(identifiable,  Module.TestIdent)
//        val module2: IdentifiableModule = asIdentifiableModule("SourceName", "ComponentName",  Module.TestIdent)
//
//        val module3: IdentifiableModule = asIdentifiableModule("", "ComponentName", Module.TestIdent)
//
//        assertEquals("ComponentName[SourceName]", identifiable.completeName)
//        assertEquals("TestIdentifiable", module.moduleName.toString())
//        assertEquals("TestIdentifiable[ComponentName[SourceName]]", module.completeName)
//        assertEquals("TestIdentifiable[ComponentName]", module3.completeName)
//
//        assertEquals("TestIdentifiable[ComponentName[SourceName]]", module.toString())
//
//        assertEquals(module, module2)

    }

}