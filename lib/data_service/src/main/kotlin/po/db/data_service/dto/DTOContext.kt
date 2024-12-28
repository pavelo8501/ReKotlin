package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.CanNotify
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.models.DTOResult
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceContext



class DTOContext<SERVICE_ENTITY, DTO_ENTITY> (
    private val dtoModel : DTOClass<DTO_ENTITY>
) : CanNotify where   SERVICE_ENTITY : LongEntity, DTO_ENTITY:LongEntity {

    private val serviceContext : ServiceContext<SERVICE_ENTITY>?= null

   override val name: String =  "DTOContext"

   override val  notificator: Notificator = Notificator(this)

    init {
        println("DTOContext| $name Initialized")
    }

    fun  DTOContext<SERVICE_ENTITY, DTO_ENTITY>.select(block: DTOContext<SERVICE_ENTITY, DTO_ENTITY>.(DTOClass<DTO_ENTITY>) -> Unit): DTOResult<DTO_ENTITY>? {
        try {
            serviceContext?.daoFactory?.all(dtoModel)?.forEach { daoEntity ->
               // val commonDTO = dtoModel.create(daoEntity)

               // val dtoList = daoEntities.map { dtoModel.create(it) }
            }
            this.block(dtoModel)
            return DTOResult(dtoModel)
        }catch (ex:Exception){
            println(ex.message)
            return null
        }
    }

    fun DTOContext<SERVICE_ENTITY, DTO_ENTITY>.update(block: DTOContext<SERVICE_ENTITY, DTO_ENTITY>.(DTOClass<DTO_ENTITY>) -> Unit): DTOResult<DTO_ENTITY>? {
        try {
            val unsaved = dtoModel.dtoContainer.filter { it.getId() == 0L }
            serviceContext?.daoFactory.apply {
                unsaved.forEach {
                    this?.new(it, dtoModel) ?: throw OperationsException("Failed to update dto ${it.className}", ExceptionCodes.ENTITY_UPDATE_FAILURE)
                }
            }
            this.block(dtoModel)
            return DTOResult(dtoModel)
        }catch (ex:Exception){
            println(ex.message)
            return null
        }
    }


}