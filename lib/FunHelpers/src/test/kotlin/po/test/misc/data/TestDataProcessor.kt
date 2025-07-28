package po.test.misc.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.processors.DataProcessor
import po.misc.functions.dsl.helpers.nextBlock
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestDataProcessor {

   internal data class TopDataItem (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<TopDataItem>(this){

        override val self: TopDataItem = this

        //override val producer: CTX = identifiable(personalName, null).context
    //    override val itemId: ValueBased = toValueBased(id)

        companion object: PrintableCompanion<TopDataItem>({TopDataItem::class}){

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

    internal data class SubData (
        val id: Int,
        val personalName: String,
        val content : String,
        val componentName: String = "Some name",
    ): PrintableBase<SubData>(this){

        override val self: SubData = this
        companion object:PrintableCompanion<SubData>({SubData::class}){

            val SubTemplate = createTemplate {
                nextBlock {
                    "Template1->$content"
                }
            }
        }
    }

    @Test
    fun `DataProcessor templated string`(){

        val topDataProcessor: DataProcessor<TopDataItem> = DataProcessor<TopDataItem>(null)

        val subRecord = SubData(1, "Name", "subRecord content")
        topDataProcessor.logData<SubData>(subRecord, SubData.SubTemplate)
    }

    @Test
    fun `Data propagated to topDataProcessor and stacked within PrintableBase`(){

        val topDataProcessor: DataProcessor<TopDataItem> = DataProcessor<TopDataItem>(null)

        //val subDataProcessor: DataProcessor<SubData> = DataProcessor<SubData>(topDataProcessor)

        var parentRecord: PrintableBase<*>? = null
        var topRecord : PrintableBase<*>? = null

        topDataProcessor.hooks.dataReceived{topRecord = it }
        topDataProcessor.hooks.childAttached {childRec, record -> parentRecord = record   }

        val newTopRecord = TopDataItem(1, "TopDataItem", "TopDataItem_Content1")
        topDataProcessor.processRecord(newTopRecord, TopDataItem.TopTemplate)

        val record1 = SubData(1, "DataItem", "Content1")
        val record2 = SubData(2, "DataItem", "Content2")

      //  subDataProcessor.forwardOrEmmit(record1)
       // subDataProcessor.forwardOrEmmit(record2)

        val activeTopRecord = assertNotNull(topRecord, "TopRecord is null")
        val activeTopDataItem = assertInstanceOf<TopDataItem>(activeTopRecord, "ActiveTopRecord is not an instance of TopDataItem")
        assertEquals("TopDataItem_Content1", activeTopDataItem.content, "Wrong content in activeTopDataItem")

        val processedData = assertNotNull(parentRecord, "ParentRecord is null")
        assertEquals(2, processedData.children.size, "Child records were not attached")
    }


    @Test
    fun `Built in debug method work as expected`(){
        val topDataProcessor: DataProcessor<TopDataItem> = DataProcessor(null)
       // val subDataProcessor: DataProcessor<SubData> = DataProcessor<SubData>(topDataProcessor)

        val debugInfo = TopDataItem(0, "Some name", "Content")
        val subDebugInfo = SubData(0, "Sub Data", "sub Content")
        var toDebug:TopDataItem? = null
        var toDebugSub: SubData? = null

        topDataProcessor.debugData(debugInfo, TopDataItem, TopDataItem.Debug){debuggable->
            toDebug = debuggable
        }
        assertNull(toDebug, "Debuggable lambda invoked while not registered")

        topDataProcessor.allowDebug(TopDataItem)
        topDataProcessor.debugData(debugInfo, TopDataItem, TopDataItem.Debug){debuggable->
            toDebug = debuggable
            debuggable.echo()
        }
        assertNotNull(toDebug, "Debuggable lambda not invoked when class is white-listed")
        toDebug = null
        topDataProcessor.allowDebug(SubData)
        topDataProcessor.debugData(debugInfo, TopDataItem, TopDataItem.Debug){debuggable->
            toDebug = debuggable
        }
        topDataProcessor.debugData(subDebugInfo, SubData, SubData.SubTemplate){ debuggable->
            toDebugSub = debuggable
        }
        assertNotNull(toDebug, "Debuggable lambda not invoked when TopDataItem class is white-listed")
        assertNotNull(toDebugSub, "Debuggable lambda not invoked when SubData class is white-listed")
        toDebugSub = null

//        subDataProcessor.debugData(subDebugInfo, SubData, SubData.SubTemplate){debuggable->
//            toDebugSub = debuggable
//        }
        assertNotNull(toDebugSub, "Debug allowance is not propagated to subDataProcessor")

    }
}