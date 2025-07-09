package po.exposify.dto.components.tracker

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.common.events.DTOData
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.tracker.extensions.resolveHierarchy
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.components.tracker.models.DTOEvents
import po.exposify.dto.components.tracker.models.TrackerConfig
import po.exposify.dto.components.tracker.models.TrackerTag
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.TasksManaged
import po.lognotify.process.LogReceiver
import po.misc.callbacks.CallableContainer
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.Containable
import po.misc.callbacks.builders.callbackBuilder
import po.misc.callbacks.builders.createPayload
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.printableProxy
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.CtxId
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiable
import po.misc.lookups.HierarchyNode
import po.misc.lookups.transformNode
import po.misc.reflection.properties.models.PropertyUpdate
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.startTimer

class DTOTracker<DTO: ModelDTO, D: DataModel, E: LongEntity>(
    internal val dto : CommonDTO<DTO, D, E>
):  MeasuredContext, TrackableDTO, TasksManaged, IdentifiableClass{

    override val identity: ClassIdentity = ClassIdentity.create("Tracker", dto.sourceName)

    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(dto.completeName, "-1")

    @PublishedApi internal var config : TrackerConfig = TrackerConfig()

    var activeRecord : TrackerRecord = TrackerRecord(this, CrudOperation.Create, dto.completeName)
        private set

    val trackRecords : MutableList<TrackerRecord> = mutableListOf(activeRecord)
    override val childTrackers : MutableList<TrackableDTO> = mutableListOf()
    private val debug = printableProxy(this, DTOData.Debug){ params->
        debug(DTOData(this, params.message), DTOData, params.template)
    }

    internal val notifier: CallbackManager<DTOEvents> = callbackBuilder<DTOEvents> {
        createPayload<DTOEvents, DTOTracker<DTO, *, *>>(DTOEvents.RootDtosCreated)
        createPayload<DTOEvents, DTOTracker<DTO, *, *>>(DTOEvents.OnUpdate)
        createPayload<DTOEvents, DTOTracker<DTO, *, *>>(DTOEvents.OnCRUDComplete)
    }

    internal fun dtoIdUpdated(id: Long){
        identity.provideId(id)
    }

    fun updateConfig(trackerConfig:TrackerConfig){
        config = trackerConfig
    }

    internal fun subscribe(
        event:DTOEvents,
        ctx: IdentifiableContext?,
        lambda: (Containable<DTOTracker<DTO, *, *>>) -> Unit
    ){
        val context = ctx?:run {
            asIdentifiable("Default", "Default")
        }
        notifier.subscribe<DTOTracker<DTO, *, *>>(context, event, lambda)
    }

    internal fun logDebug(message: String, byContext: CtxId){
        val toDebugMsg = "$message by ${byContext.contextName}"
        debug.logMessage(toDebugMsg)
    }

    private fun finalizeLast(){
        executionTimeStamp.stopTimer()
        activeRecord.finalizeRecord()
        trackRecords.add(activeRecord)
        if(trackRecords.size > 1){ onComplete() }
    }

    private fun onStart(){
        notifier.trigger(DTOEvents.OnUpdate, this)
    }
    private fun onComplete(){
        notifier.trigger(DTOEvents.OnCRUDComplete, this)
    }


    fun resolveHierarchy(): HierarchyNode<TrackableDTO>{
        val dtoNode =   dto.resolveHierarchy()
        val transformed = transformNode(dtoNode){dto ->
            (dto).tracker as TrackableDTO
        }
        return transformed
    }

    fun propertyUpdated(update: List<PropertyUpdate<*>>){
        activeRecord.setPropertyUpdate(update)
    }

    fun addTrackInfo(operation:CrudOperation, module: IdentifiableContext? = null):DTOTracker<DTO, D, E>{
        finalizeLast()
        if(activeRecord.operation != CrudOperation.Create){ onStart() }

        activeRecord = TrackerRecord(this, operation, module?.contextName?:"")
        debug.logMessage("Active CRUD: $activeRecord", DTOData.Stats)
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







