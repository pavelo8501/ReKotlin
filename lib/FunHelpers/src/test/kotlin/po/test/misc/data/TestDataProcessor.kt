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
    ): PrintableBase<TopDataItem>(){

        override val self: TopDataItem = this

        override val emitter: Identifiable = asIdentifiable(personalName, componentName)
        override val itemId: ValueBased = toValueBased(id)

        companion object{
            val TopTemplate: PrintableTemplate<TopDataItem> = PrintableTemplate{"TopTemplate->$content"}
        }
    }

    data class DataItem (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<DataItem>(){

        override val self: DataItem = this

        override val emitter: Identifiable = asIdentifiable(personalName, componentName)
        override val itemId: ValueBased = toValueBased(id)

        companion object{
            val Template1: PrintableTemplate<DataItem> = PrintableTemplate{"Template1->$content"}
        }
    }

    val topDataProcessor: DataProcessor = DataProcessor()
    val subDataProcessor: DataProcessor = DataProcessor(topDataProcessor)


    @Test
    fun `Data propagated to topDataProcessor and stacked within PrintableBase`(){

        var parentRecord: PrintableBase<*>? = null
        var topRecord : PrintableBase<*>? = null

        topDataProcessor.onRecordAttached {topRecord = it }
        topDataProcessor.onChildAttached{childRec, record -> parentRecord = record }


        val newTopRecord = TopDataItem(1, "TopDataItem", "TopDataItem_Content1")
        topDataProcessor.processRecord(newTopRecord, TopDataItem.TopTemplate)

        val record1 = DataItem(1, "DataItem", "Content1")
        val record2 = DataItem(2, "DataItem", "Content2")
        subDataProcessor.forwardTop(record1)
        subDataProcessor.forwardTop(record2)

        val activeTopRecord = assertNotNull(topRecord, "TopRecord is null")
        val activeTopDataItem = assertInstanceOf<TopDataItem>(activeTopRecord, "ActiveTopRecord is not an instance of TopDataItem")
        assertEquals("TopDataItem_Content1", activeTopDataItem.content, "Wrong content in activeTopDataItem")

        val processedData = assertNotNull(parentRecord, "ParentRecord is null")
        assertEquals(2, processedData.children.size, "Child records were not attached")

    }


}