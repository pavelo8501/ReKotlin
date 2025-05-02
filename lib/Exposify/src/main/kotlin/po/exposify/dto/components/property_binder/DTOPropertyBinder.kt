package po.exposify.dto.components.property_binder

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.delegates.ComplexDelegate
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.extensions.castOrOperationsEx

class DTOPropertyBinder<DTO, DATA, ENTITY>(
    val dto : CommonDTO<DTO, DATA, ENTITY>
) where  DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntity
{

    val bindingMap : MutableMap<String,  ComplexDelegate<DTO, DATA, ENTITY, *, *, *>> = mutableMapOf()

    fun <PARENT_ENTITY : ExposifyEntity, DATA_VAL, RES_VAL> setBinding(
        binding: ComplexDelegate<DTO, DATA, ENTITY, PARENT_ENTITY, DATA_VAL, RES_VAL>)
    : ComplexDelegate<DTO, DATA, ENTITY, PARENT_ENTITY, DATA_VAL, RES_VAL>
    {
        bindingMap[binding.qualifiedName] = binding
        return binding
    }

    suspend fun <PE: ExposifyEntity> beforeInsertUpdate(
        dataModel: DATA,
        entityContainer : EntityUpdateContainer<ENTITY, *, *, PE>,
        updateMode: UpdateMode)
    {
        val container =  entityContainer.castOrOperationsEx<EntityUpdateContainer<ENTITY, *, *, ExposifyEntity>>()
        bindingMap.values.forEach { it.entityBeforeInsertedUpdate(updateMode, container) }
    }

    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: ExposifyEntity> afterInsertUpdate(
        dataModel: DATA,
        entityContainer : EntityUpdateContainer<ENTITY, P_DTO, PD, PE>,
        updateMode: UpdateMode)
    {
        bindingMap.values.forEach { it.entityAfterInsertedUpdate(updateMode, entityContainer) }
    }

}

