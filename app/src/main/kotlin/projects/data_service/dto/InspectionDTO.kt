package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOModel
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.playground.projects.data_service.services.Inspections
import po.playground.projects.data_service.services.Partners


class InspectionEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<InspectionEntity>(Inspections)
    var time by Inspections.time
    var created by Partners.created
    var updated by Partners.updated
    var department by DepartmentEntity referencedOn Inspections.department
}

data class InspectionDataModel(
    var time: LocalDateTime,
): DataModel {
    override var id: Long = 0L
    var updated: LocalDateTime = InspectionDTO.nowTime()
    var created: LocalDateTime = InspectionDTO.nowTime()
}

class InspectionDTO (
    override val dataModel: InspectionDataModel,
    ): CommonDTO<InspectionDataModel, InspectionEntity>(dataModel), DTOModel{

        companion object: DTOClass<InspectionDataModel, InspectionEntity>(InspectionDTO::class) {
            override fun setup() {
                dtoSettings<InspectionDataModel, InspectionEntity>(InspectionEntity){
                    propertyBindings(
                        PropertyBinding(InspectionDataModel::time, InspectionEntity::time)
                    )
                }
            }
        }
}