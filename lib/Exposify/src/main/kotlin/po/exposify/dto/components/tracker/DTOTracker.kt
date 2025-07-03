package po.exposify.dto.components.tracker

import po.exposify.common.events.DTOEvent
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.TasksManaged
import po.misc.data.printable.printableProxy
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.lookups.HierarchyNode
import po.misc.lookups.transformNode
import po.misc.reflection.properties.models.PropertyUpdate
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.startTimer

class DTOTracker<DTO: ModelDTO, DATA: DataModel>(
    internal val dto : CommonDTO<DTO, DATA, *>,
   @PublishedApi internal val config : TrackerConfig = TrackerConfig()
):  MeasuredContext, TrackableDTO, TasksManaged, IdentifiableClass
{
    override val identity: ClassIdentity = ClassIdentity.create("Tracker", dto.sourceName, dto.id)
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(dto.completeName, dto.id.toString())

    var activeRecord : TrackerRecord = TrackerRecord(this, CrudOperation.Create, dto.completeName)
        private set

    val trackRecords : MutableList<TrackerRecord> = mutableListOf(activeRecord)
    override val childTrackers : MutableList<TrackableDTO> = mutableListOf()

    val debug = printableProxy(this, DTOEvent.Debug){params->
        debug(DTOEvent(this, params.message), DTOEvent, params.template)
    }

    private fun finalizeLast(){
        executionTimeStamp.stopTimer()
        activeRecord.finalizeRecord()
        trackRecords.add(activeRecord)
    }

    fun resolveHierarchy(): HierarchyNode<TrackableDTO>{
        val dtoNode = dto.bindingHub.resolveHierarchy()
        val transformed = transformNode(dtoNode){dto ->
            (dto).tracker as TrackableDTO
        }
        return transformed
    }

    fun propertyUpdated(update: List<PropertyUpdate<*>>){
        activeRecord.setPropertyUpdate(update)
    }

    fun addTrackInfo(operation:CrudOperation, module: IdentifiableContext? = null):DTOTracker<DTO, DATA>{
        finalizeLast()
        activeRecord = TrackerRecord(this, operation, module?.contextName?:"")
        debug.logMessage("Active CRUD: $activeRecord", DTOEvent.Stats)
        startTimer()
        return this
    }

    fun addTrackResult(operation:CrudOperation? = null) {
        trackRecords.firstOrNull { it.operation == operation }?.finalizeRecord()?:finalizeLast()
    }

    fun TrackableDTO.getTrace(indent: String = "", isLast: Boolean = true): String {
        val connector = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
        val line = "$indent$connector[$completeName] (${trackRecords.size} changes)"
        val nextIndent = indent + if (isLast) "    " else "│   "
        val childLines = childTrackers.mapIndexed { index, child ->
            child.getTrace(nextIndent, index == childTrackers.lastIndex)
        }
        return listOf(line, *childLines.toTypedArray()).joinToString("\n")
    }

    fun printTrace(){
        println(getTrace())
    }

    override fun toString(): String {
        return "DTO(${dto.sourceName}#${dto.id})"
    }
}







