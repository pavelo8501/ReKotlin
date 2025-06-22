package po.test.misc.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.processors.DataProcessor
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestDataProcessor {

    data class TopDataItem (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<TopDataItem>(TopTemplate){

        override val self: TopDataItem = this

        override val emitter: Identifiable = asIdentifiable(personalName, componentName)
        override val itemId: ValueBased = toValueBased(id)

        companion object{
            val TopTemplate: PrintableTemplate<TopDataItem> = PrintableTemplate("TopTemplate"){"TopTemplate->$content"}
        }
    }

    data class SubData (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<SubData>(SubTemplate){

        override val self: SubData = this

        override val emitter: Identifiable = asIdentifiable(personalName, componentName)
        override val itemId: ValueBased = toValueBased(id)

        companion object{
            val SubTemplate: PrintableTemplate<SubData> = PrintableTemplate("SubTemplate"){"Template1->$content"}
        }
    }

    val topDataProcessor: DataProcessor<TopDataItem> = DataProcessor<TopDataItem>()
    val subDataProcessor: DataProcessor<SubData> = DataProcessor<SubData>(topDataProcessor)

    @Test
    fun `DataProcessor templated string`(){
        val subRecord = SubData(1, "Name", "subRecord content")
        topDataProcessor.logData<SubData>(subRecord, SubData.SubTemplate)
    }

    @Test
    fun `Data propagated to topDataProcessor and stacked within PrintableBase`(){

        var parentRecord: TopDataItem? = null
        var topRecord : PrintableBase<*>? = null

        topDataProcessor.hooks.dataReceived{topRecord = it }
        topDataProcessor.onChildAttached{childRec, record -> parentRecord = record }

        val newTopRecord = TopDataItem(1, "TopDataItem", "TopDataItem_Content1")
        topDataProcessor.processRecord(newTopRecord, TopDataItem.TopTemplate)

        val record1 = SubData(1, "DataItem", "Content1")
        val record2 = SubData(2, "DataItem", "Content2")

        subDataProcessor.forwardTop(record1)
        subDataProcessor.forwardTop(record2)

        val activeTopRecord = assertNotNull(topRecord, "TopRecord is null")
        val activeTopDataItem = assertInstanceOf<TopDataItem>(activeTopRecord, "ActiveTopRecord is not an instance of TopDataItem")
        assertEquals("TopDataItem_Content1", activeTopDataItem.content, "Wrong content in activeTopDataItem")

        val processedData = assertNotNull(parentRecord, "ParentRecord is null")
        assertEquals(2, processedData.children.size, "Child records were not attached")
    }


}