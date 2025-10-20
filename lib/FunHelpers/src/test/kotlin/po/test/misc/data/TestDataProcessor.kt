package po.test.misc.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.processors.DataProcessor
import po.misc.functions.dsl.helpers.nextBlock
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestDataProcessor {

   internal data class TopData (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<TopData>(this){

        override val self: TopData = this

        companion object: PrintableCompanion<TopData>(TypeToken.create()){
            val TopTemplate = createTemplate {
                nextBlock {
                    "TopTemplate->$content"
                }
            }
            val Debug = createTemplate {
                nextBlock {
                    "Debug[Name:$personalName Content:$content"
                }
            }
        }
    }

    internal data class ForeignData (
        val id: Int,
        val personalName: String,
        val content : String
    ): PrintableBase<ForeignData>(this){
        override val self: ForeignData = this
        companion object:PrintableCompanion<ForeignData>(TypeToken.create()){
            val Template = createTemplate {
                nextBlock {
                    "Template1->$content"
                }
            }
        }
    }

    internal data class SubData (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<SubData>(this){
        override val self: SubData = this
        companion object:PrintableCompanion<SubData>(TypeToken.create()){
            val SubTemplate = createTemplate {
                nextBlock {
                    "Template1->$content"
                }
            }
        }
    }

    private fun createTopData(id: Int): TopData{
      return  TopData(id, "TopData_$id", "Content_$id")
    }

    private fun createForeignData(id: Int): ForeignData{
        return  ForeignData(id, "ForeignData_$id", "ForeignData_$id")
    }

    @Test
    fun `Arbitrary data stacked properly`(){
        val dataProcessor: DataProcessor<TopData> = DataProcessor(null)
        val topData1 = createTopData(1)
        val topData2 = createTopData(2)

        dataProcessor.addData(topData1)
        dataProcessor.addData(topData2)

        val foreignData1 = createForeignData(1)
        val foreignData2 = createForeignData(2)

        dataProcessor.addArbitraryData(foreignData1)
        dataProcessor.addArbitraryData(foreignData2)

        assertEquals(2, dataProcessor.records.size)
        assertSame(topData2, dataProcessor.activeRecord)
        assertEquals(2, topData2.arbitraryMap.totalSize)
    }

    @Test
    fun `Data records propagation from bottom to top work as expected`(){

        val topProcessor: DataProcessor<TopData> = DataProcessor(null)
        val subProcessor : DataProcessor<TopData> = DataProcessor(topProcessor)

        val subData = createTopData(1)
        val subData2 = createTopData(2)

        subProcessor.addData(subData)
        subProcessor.addData(subData2)

        assertEquals(2, topProcessor.records.size)
        assertEquals(2, subProcessor.records.size)

        assertSame(subData, topProcessor.records[0])
        assertSame(subData2, topProcessor.records[1])

        assertSame(subData, subProcessor.records[0])
        assertSame(subData2, subProcessor.records[1])
    }

    @Test
    fun `Check processors hierarchical hooks usage`(){

        val topProcessor: DataProcessor<TopData> = DataProcessor()
        val subProcessor : DataProcessor<TopData> = DataProcessor(topProcessor)

        val subData = createTopData(1)
        val subData2 = createTopData(2)

        val dataReceivedList: MutableList<Any> = mutableListOf()
        topProcessor.onDataReceived {
            dataReceivedList.add(it)
        }

        val subDataReceivedList: MutableList<TopData> = mutableListOf()
        topProcessor.onSubDataReceived {data, processor->
            assertSame(subProcessor, processor)
            subDataReceivedList.add(data)
        }
        subProcessor.addData(subData)
        subProcessor.addData(subData2)

        assertTrue(dataReceivedList.isEmpty())
        assertEquals(2, subDataReceivedList.size)

    }

}