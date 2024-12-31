package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.interfaces.CanNotify
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOFactory
import po.db.data_service.dto.models.DTOResult
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.HostableDTO
import po.db.data_service.scope.service.ServiceContext
import po.db.data_service.scope.service.models.DaoFactory


class DTOContext<SERVICE_ENTITY, DTO_ENTITY> (
    private val dtoModel :   DTOClass<DTO_ENTITY>,
    private val daoFactory:  DaoFactory,
    private val dtoFactory:  DTOFactory<DTO_ENTITY>,
) : CanNotify where   SERVICE_ENTITY : LongEntity, DTO_ENTITY:LongEntity {
    private val serviceContext : ServiceContext<SERVICE_ENTITY>?= null
    override val name: String =  "DTOContext"
    override val  notificator: Notificator = Notificator(this)

    init {
        println("DTOContext| $name Initialized")
    }

    private fun insert(dto: HostableDTO<DTO_ENTITY>): DTO_ENTITY?{
        try {
           val ent = daoFactory.dbQuery {
                val daoEntity = dtoModel.daoModel.new {
                    dto.updateDTO(this, UpdateMode.MODEL_TO_ENTITY)
                }
                dtoModel.conf.relationBinder.bindings().forEach { binding ->
                    binding.attachToParent(daoEntity, dto)
                }
               daoEntity
            }
            return ent
        }catch (ex:Exception){
            println(ex.message)
            return null
        }
    }

    fun  DTOContext<SERVICE_ENTITY, DTO_ENTITY>.select(block: DTOContext<SERVICE_ENTITY, DTO_ENTITY>.(DTOClass<DTO_ENTITY>) -> Unit): DTOResult<DTO_ENTITY>? {
        try {
//            serviceContext?.daoFactory?.select(dtoModel)?.forEach { daoEntity ->
//               // val commonDTO = dtoModel.create(daoEntity)
//
//               // val dtoList = daoEntities.map { dtoModel.create(it) }
//            }
            this.block(dtoModel)
            return DTOResult(dtoModel)
        }catch (ex:Exception){
            println(ex.message)
            return null
        }
    }

    fun DTOContext<SERVICE_ENTITY, DTO_ENTITY>.update(entities:List<HostableDTO<DTO_ENTITY>>, block: DTOContext<SERVICE_ENTITY, DTO_ENTITY>.(DTOClass<DTO_ENTITY>) -> Unit): DTOResult<DTO_ENTITY>? {
        try {
            entities.filter { it.isNew }.forEach {  }
            entities.forEach {
                when(it.isNew){
                    true->{
                        insert(it).let { }
                    }
                    false->{
                        daoFactory.update(it,dtoModel).let {}
                    }
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