package po.misc.data.logging.processor.contracts

import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.logging.procedural.ProceduralFlow




class ProceduralContract(val templateRecord: LoggableTemplate) {

    var newContextCreatesNewNode: Boolean = true
    var consecutiveCreatesNewEntry: Boolean = true

    private fun processLastReceived(
        received: StructuredLoggable,
        lastTemplate: LoggableTemplate
    ){
        if(received.context !== lastTemplate.context){
            val newProcedural = ProceduralFlow.toProceduralRecord(received, topNode = false)
            lastTemplate.logRecord.addRecord(newProcedural.logRecord)
            lastTemplate.addRecord(newProcedural)
        } else {
            if(consecutiveCreatesNewEntry){
                val entry =  recordCreatesEntry(lastTemplate, received)
                lastTemplate.addEntry(entry)
            }else{
                lastTemplate.addMessage(received)
            }
        }
    }

    private fun recordCreatesEntry(parentProcedural: LoggableTemplate,  record: StructuredLoggable): ProceduralEntry{
        return ProceduralFlow.createEntry(parentProcedural, record)
    }

    fun addRecord(data: StructuredLoggable): StructuredLoggable {
        val lastReceived =   templateRecord.getRecord()
        processLastReceived(data, lastReceived)
        return data
    }

    fun addRecords(records: List<StructuredLoggable>): List<StructuredLoggable> {
        records.forEach {
            addRecord(it)
        }
        return records
    }


}