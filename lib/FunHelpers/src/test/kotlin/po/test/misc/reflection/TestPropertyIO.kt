package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.reflection.objects.Composed
import po.misc.reflection.properties.PropertyGroup
import po.misc.reflection.properties.createPropertyIO
import po.misc.reflection.properties.createSourcePropertyIO
import kotlin.test.assertEquals

class TestPropertyIO {

    data class UpdateSource(val field1: String = "Updated Value")
    data class DBEntity(var field1: String)
    class Source(var field1: String = "field1"): IdentifiableContext{
        override val contextName: String
            get() = "Source"
    }
//
//    @Test
//    fun `Three way data binding work as expected`(){
//
//        val source = Source()
//
//        val sourceProperty = createSourcePropertyIO(source, Source::field1, String::class)
//        val group = PropertyGroup(sourceProperty)
//
//        val dataProperty = createPropertyIO(UpdateSource::field1, UpdateSource::class)
//        val dataSlot = group.addProperty(dataProperty)
//
//        val entity = DBEntity("")
//        val entityProperty = createPropertyIO(DBEntity::field1, entity)
//        val entitySlot = group.addProperty(entityProperty)
//
//        val update = UpdateSource()
//        dataSlot.provideData(update){
//            entitySlot.setValue(it)
//            sourceProperty.setValue(it)
//        }
//
//        assertEquals(update.field1, source.field1, "Source class field1 not updated")
//        assertEquals(update.field1, entity.field1, "DBEntity class field1 not updated")
//
//    }
}