package po.exposify.dto.components.tracker

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.property_binder.interfaces.ObservableData
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.start
import po.misc.time.stop


class DTOTracker<DTO: ModelDTO, DATA: DataModel>(
    internal val dto : CommonDTO<DTO, DATA, *>,
   @PublishedApi internal val config : TrackerConfig = TrackerConfig()
): MeasuredContext, TrackableDTO {

    override val name : String get() = config.name?:dto.dtoName
    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(dto.dtoName, dto.id.toString())
    private var activeRecord : TrackerRecord = TrackerRecord(dto.id, CrudOperation.Create, name)
    private val trackRecords : MutableList<TrackerRecord> = mutableListOf()

    override val records : List<ObservableData> get() = trackRecords.toList()
    override val childTrackers : MutableList<TrackableDTO> = mutableListOf()


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
        if(activeRecord.operation == CrudOperation.Initialize){
            addTrackResult(CrudOperation.Initialize)
        }
        activeRecord = TrackerRecord(dto.id, operation, module?.qualifiedName?:"")
        trackRecords.add(activeRecord)
        return start()
    }

    fun addTrackResult(operation:CrudOperation? = null) {

        trackRecords.firstOrNull { it.operation == operation }?.finalize(dto, stop(dto.id.toString()))?:finalizeLast()
    }


    private fun collectTrackersInto(parentDto : CommonDTO<*, *, *>) {
        parentDto.getDtoRepositories().forEach {repo->
            when(repo){
                is SingleRepository->{
                    repo.getDTO().let {
                        parentDto.tracker.childTrackers.add(it.tracker)
                        collectTrackersInto(it)
                    }
                }
                is MultipleRepository->{
                    val list =  repo.getDTO()
                    if(list.isNotEmpty()){
                        list.forEach {
                            parentDto.tracker.childTrackers.add(it.tracker)
                            collectTrackersInto(it)
                        }
                    }
                }
            }
        }
    }

    fun collectTrackers(): TrackableDTO {
        collectTrackersInto(this.dto)
        return this
    }

    fun TrackableDTO.getTrace(indent: String = "", isLast: Boolean = true): String {
        val connector = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
        val line = "$indent$connector[$name] (${records.size} changes)"
        val nextIndent = indent + if (isLast) "    " else "│   "
        val childLines = childTrackers.mapIndexed { index, child ->
            child.getTrace(nextIndent, index == childTrackers.lastIndex)
        }
        return listOf(line, *childLines.toTypedArray()).joinToString("\n")
    }

    fun printTrace(){
        println(getTrace())
    }
}







