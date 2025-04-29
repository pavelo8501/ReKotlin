package po.exposify.entity.classes

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.interfaces.ExposifyEntity
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast

abstract class ExposifyEntityBase(id: EntityID<Long>) : LongEntity(id) , ExposifyEntity{

    var daoService : DAOService<ModelDTO, ExposifyEntityBase>? = null
    internal fun registerService(service : DAOService<ModelDTO, ExposifyEntityBase>){
        daoService = service
    }

   var subscription : (DAOService<ModelDTO, ExposifyEntityBase>.(ExposifyEntityBase)-> Unit) ?= null


   inline  fun <reified ENTITY: ExposifyEntityBase> onEntityCreated(
       entity: ENTITY,
       block: DAOService<ModelDTO, ENTITY>.(ENTITY)-> Unit
   ){
       val service = daoService.getOrOperationsEx()
       service.safeCast<DAOService<ModelDTO, ENTITY>>()?.let {
           block.invoke(it, entity)

       }?:run {
           println("Subscription invalid")
           subscription = null
       }
    }

}