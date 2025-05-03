package po.exposify.dto.models

import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.start
import po.misc.time.stop


enum class CrudOperation{
    Created,
    Select,
    Save,
    Update,
    Pick,
    Delete
}

fun ModelDTO.trackSave(
    operation:CrudOperation,
    moduleName: IdentifiableComponent
):DTOTracker<*,*>{
   return dtoTracker.addTrackInfo(operation, moduleName)
}

data class DTOTracker<DTO: ModelDTO, DATA: DataModel>(
    private val dto : CommonDTO<DTO,DATA,*>,
    val crudOperation : CrudOperation = CrudOperation.Created,
    val waypoint: String = "",
): MeasuredContext {

    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(dto.dtoName, dto.id.toString())
    private var result: Int = 0

    private val trackRecords : MutableList<DTOTracker<*,*>> = mutableListOf()
    val records : List<DTOTracker<*,*>> get() = trackRecords.toList()

    fun addTrackInfo(operation:CrudOperation, moduleName: IdentifiableComponent):DTOTracker<*,*>{
        val newTrackerRec = DTOTracker(dto, operation, moduleName.name)
        trackRecords.add(newTrackerRec)
        return newTrackerRec.start()
    }

    fun addTrackInfoResult(trackResult: Int) {
        result = trackResult
        stop()
    }

    fun getTrace(indent: String = "", isLast: Boolean = true): String {
        val connector = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
        val line = "$indent$connector[${dto.dtoName}] $crudOperation($result)"

        val nextIndent = indent + if (isLast) "    " else "│   "

        val childLines = records.mapIndexed { index, child ->
            child.getTrace(nextIndent, index == records.lastIndex)
        }
        return listOf(line, *childLines.toTypedArray()).joinToString("\n")
    }

    fun printTrace(){
        println(getTrace())
    }

}