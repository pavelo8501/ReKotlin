package po.exposify.dto.components.property_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.delegates.ComplexDelegate
import po.exposify.dto.components.property_binder.delegates.ForeignIDClassDelegate
import po.exposify.dto.components.property_binder.delegates.PropertyDelegate
import po.exposify.dto.components.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperationsEx

class DTOPropertyBinder<DTO, DATA, ENTITY>(
    val dto : CommonDTO<DTO, DATA, ENTITY>
) where  DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity
{

    private val complexDelegateMap : MutableMap<String,  ComplexDelegate<DTO, DATA, ENTITY, *, *, *, *, *>> = mutableMapOf()
    private val responsiveDelegates : MutableList<ResponsiveDelegate<DTO, DATA, ENTITY, *>>  = mutableListOf()



    fun <FE : LongEntity> setBinding(
        binding: ComplexDelegate<DTO, DATA, ENTITY,* , *, FE, *, *>)
    : ComplexDelegate<DTO, DATA, ENTITY, *, *, FE, *, *>
    {
        complexDelegateMap[binding.qualifiedName] = binding

        return binding
    }

    fun setBinding(responsiveDelegate : ResponsiveDelegate<DTO, DATA, ENTITY, *>){
        responsiveDelegates.add(responsiveDelegate)
    }

    fun update(model:DATA){
        responsiveDelegates.forEach { it.update(model) }
    }

    suspend fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> update(
        container : EntityUpdateContainer<ENTITY, F_DTO, FD, FE>
    ){

        if(container.updateMode == UpdateMode.ENTITY_TO_MODEL && container.inserted){
            val id = container.ownEntity.id.value
            dto.id =  id
            dto.dataModel.id = id
        }

        responsiveDelegates.forEach { it.update(container) }
        complexDelegateMap.values.forEach { it.beforeInsertedUpdate(container) }
    }

    suspend fun <P_DTO: ModelDTO, PD: DataModel, FE: LongEntity> afterInsertUpdate(
        entityContainer : EntityUpdateContainer<ENTITY, P_DTO, PD, FE>)
    {
        complexDelegateMap.values.forEach { it.afterInsertedUpdate(entityContainer) }
    }

}

