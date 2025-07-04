package po.exposify.dto.components.tracker

import po.exposify.dto.CommonDTO
import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.components.tracker.interfaces.TrackableDTO
import po.exposify.dto.enums.Components
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiable
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import po.misc.time.startTimer
import po.misc.time.stopTimer


class DTOTracker<DTO: ModelDTO, DATA: DataModel>(
    internal val dto : CommonDTO<DTO, DATA, *>,
   @PublishedApi internal val config : TrackerConfig = TrackerConfig(),
):  MeasuredContext, TrackableDTO{

    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(dto.completeName, dto.id.toString())
    override var sourceName: String = dto.sourceName
    override val contextName: String = "DTOTracker"

    private var activeRecord : TrackerRecord = TrackerRecord(this, dto.id, CrudOperation.Create, dto.completeName)
    private val trackRecords : MutableList<TrackerRecord> = mutableListOf()



    override val records : List<ObservableData> get() = trackRecords.toList()
    override val childTrackers : MutableList<TrackableDTO> = mutableListOf()

    private fun finalizeLast(){
        activeRecord.finalize(dto,stopTimer())
    }

    fun propertyUpdated(update: ObservableData){
        activeRecord.addAction(update)
    }
    fun relationPropertyUpdated(update: ObservableData){
        activeRecord.addAction(update)
    }
    fun addTrackInfo(operation:CrudOperation, module: IdentifiableContext? = null):DTOTracker<DTO, DATA>{
        if(activeRecord.operation == CrudOperation.Initialize){
            addTrackResult(CrudOperation.Initialize)
        }
        activeRecord = TrackerRecord(this, dto.id, operation, module?.contextName?:"")
        trackRecords.add(activeRecord)
        return startTimer()
    }

    fun addTrackResult(operation:CrudOperation? = null) {

        trackRecords.firstOrNull { it.operation == operation }?.finalize(dto, stopTimer())?:finalizeLast()
    }


    private fun collectTrackersInto(parentDto : CommonDTO<*, *, *>) {
//        parentDto.getDtoRepositories().forEach {repo->
//            when(repo){
//                is SingleRepository->{
//                    repo.getDTO().let {
//                        parentDto.tracker.childTrackers.add(it.tracker)
//                        collectTrackersInto(it)
//                    }
//                }
//                is MultipleRepository->{
//                    val list =  repo.getDTO()
//                    if(list.isNotEmpty()){
//                        list.forEach {
//                            parentDto.tracker.childTrackers.add(it.tracker)
//                            collectTrackersInto(it)
//                        }
//                    }
//                }
//            }
//        }

        TODO("Awaiting refactor")
    }

    fun collectTrackers(): TrackableDTO {
       // collectTrackersInto(this.dto)
        return this
    }

    fun TrackableDTO.getTrace(indent: String = "", isLast: Boolean = true): String {
        val connector = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
        val line = "$indent$connector[$completeName] (${records.size} changes)"
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







