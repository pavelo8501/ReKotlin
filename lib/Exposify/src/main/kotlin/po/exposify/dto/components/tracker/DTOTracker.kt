package po.exposify.dto.components.tracker

import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.interfaces.ObservableData
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.start
import po.misc.time.stop


class DTOTracker<DTO: ModelDTO, DATA: DataModel>(
    private val dto : CommonDTO<DTO, DATA, *>,
   @PublishedApi internal val config : TrackerConfig = TrackerConfig()
): MeasuredContext {

    val name : String get() = config.name?:dto.dtoName

    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(dto.dtoName, dto.id.toString())
    private var activeRecord : TrackerRecord = TrackerRecord(dto.id, CrudOperation.Create, name)
    private val trackRecords : MutableList<TrackerRecord> = mutableListOf()
    val records : List<TrackerRecord> get() = trackRecords.toList()

    private fun finalizeLast(){
        activeRecord.finalize(dto,stop(dto.id.toString()))
    }

    fun propertyUpdated(update: ObservableData){
        activeRecord.addAction(update)
    }
    fun relationPropertyUpdated(update: ObservableData){
        activeRecord.addAction(update)
    }
    fun addTrackInfo(operation:CrudOperation, module: IdentifiableComponent? = null):DTOTracker<DTO, DATA>{
        if(activeRecord.crudOperation == CrudOperation.Initialize){
            addTrackResult(CrudOperation.Initialize)
        }
        activeRecord = TrackerRecord(dto.id, operation, module?.qualifiedName?:"")
        trackRecords.add(activeRecord)
        return start()
    }

    fun addTrackResult(operation:CrudOperation? = null) {
        trackRecords.firstOrNull { it.crudOperation == operation }?.finalize(dto, stop(dto.id.toString()))?:finalizeLast()
    }

    fun getTrace(indent: String = "", isLast: Boolean = true): String {
//        val connector = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
//        val line = "$indent$connector[${dto.dtoName}] $crudOperation($result)"
//
//        val nextIndent = indent + if (isLast) "    " else "│   "
//
//        val childLines = records.mapIndexed { index, child ->
//            child.getTrace(nextIndent, index == records.lastIndex)
//        }
   //     return listOf(line, *childLines.toTypedArray()).joinToString("\n")
        return ""
    }

    fun printTrace(){
        println(getTrace())
    }
}

fun <DTO: ModelDTO, D: DataModel> CommonDTO<DTO, D, *>.addTrackerInfo(
    operation:CrudOperation,
    moduleName: IdentifiableComponent
):DTOTracker<DTO, D>{
    return tracker.addTrackInfo(operation, moduleName)
}

fun <DTO: ModelDTO, D: DataModel> CommonDTO<DTO, D, *>.addTrackerResult(
    operation:CrudOperation? = null
){
    return tracker.addTrackResult(operation)
}

fun CommonDTO<*, *, *>.collectTrackers(): List<DTOTracker<*, *>> {
    val result = mutableListOf<DTOTracker<*, *>>()
    collectTrackersInto(result)
    return result
}

@Suppress("UNCHECKED_CAST")
private fun CommonDTO<*, *, *>.collectTrackersInto(result: MutableList<DTOTracker<*, *>>) {
    val trackerField = this::class.members.find { it.name == "tracker" }
    val tracker = trackerField?.call(this) as? DTOTracker<*, *>
    if (tracker != null) {
        result.add(tracker)
    }

    this::class.members.forEach { member ->
        val value = runCatching { member.call(this) }.getOrNull()
        when (value) {
            is CommonDTO<*, *, *> -> value.collectTrackersInto(result)
            is List<*> -> value.filterIsInstance<CommonDTO<*, *, *>>().forEach { it.collectTrackersInto(result) }
        }
    }
}





