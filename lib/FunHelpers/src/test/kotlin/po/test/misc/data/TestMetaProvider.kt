package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.MetaDataModel
import po.misc.data.MetaProvider
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestMetaProvider {

    class Model(){
        var value1: String = ""
        var value2 : Int = 0
    }

    data class Source(val someValue: String = "SomeValue", val otherValue: String = "OtherValue", val intValue: Int = 10)

    @Test
    fun `Data model created with correct values`(){

        val model =  MetaDataModel()
        val source = Source()
        val provider = MetaProvider<MetaDataModel>({ MetaDataModel() })
//        provider.registerBuilder<Source>{source->
//            propertyInt = source.intValue
//            this
//        }
        val sourceUsed = Source()
        val newDataObject : Int = 10

    }

}