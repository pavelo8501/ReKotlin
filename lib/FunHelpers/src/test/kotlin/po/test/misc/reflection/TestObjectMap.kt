package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.reflection.objects.Composed
import po.misc.reflection.objects.ObjectManager
import po.misc.reflection.objects.builders.composable
import po.misc.reflection.objects.builders.createClassSurrogate
import kotlin.test.assertEquals

class TestObjectMap {

    enum class DataSources{
        SourceClass,
        DataClass
    }

    class MainObject(): Composed{
        internal val objectDataManager= ObjectManager<DataSources, MainObject>("TestObject",DataSources.SourceClass, DataSources::class.java)
        var intParam: Int by composable(objectDataManager)
        var message: String by composable(TestObjectData::message, objectDataManager)


    }

    data class TestObjectData(
        var message: String = "InitialString",
        var intParam: Int = 0
    ):Composed

    @Test
    fun `Object map initialization`(){
        val update1 = "Updated via delegate"
        val update2 = "Updated by data class"

        val object1 = MainObject()

        val dataManager = object1.objectDataManager
        dataManager.hooks.newMap {
            it.toString()
        }

        dataManager.hooks.propertyUpdated {
            println(it)
        }

        val dataObject = TestObjectData()
        val dataClassSurrogate =  dataObject.createClassSurrogate(DataSources.DataClass, dataManager)

        assertEquals(2, dataManager.sourceClass.size)
        assertEquals(1, dataManager.auxSurrogateMap.size)

        println(dataManager.dataInfo())

        object1.message = update1
        assertEquals(update1, object1.message, update1)

        dataObject.message = update2
        dataClassSurrogate.updateData(dataObject)

        assertEquals(update2, object1.message, update2)

    }
}