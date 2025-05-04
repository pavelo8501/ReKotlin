package po.exposify.dto.components.property_binder

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.delegates.ComplexDelegate
import po.exposify.dto.components.property_binder.delegates.ForeignIDClassDelegate
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.extensions.castOrOperationsEx

class DTOPropertyBinder<DTO, DATA, ENTITY>(
    val dto : CommonDTO<DTO, DATA, ENTITY>
) where  DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntity
{

    private val bindingMap : MutableMap<String,  ComplexDelegate<DTO, DATA, ENTITY, *, *, *>> = mutableMapOf()
    private val foreignIDClassDelegates : MutableList<ForeignIDClassDelegate<DTO, DATA, ENTITY, *>> = mutableListOf()


    fun <PARENT_ENTITY : ExposifyEntity, DATA_VAL, RES_VAL> setBinding(
        binding: ComplexDelegate<DTO, DATA, ENTITY, PARENT_ENTITY, DATA_VAL, RES_VAL>)
    : ComplexDelegate<DTO, DATA, ENTITY, PARENT_ENTITY, DATA_VAL, RES_VAL>
    {
        when(binding){
            is ForeignIDClassDelegate -> foreignIDClassDelegates.add(binding)
            else -> bindingMap[binding.qualifiedName] = binding
        }
        return binding
    }

    suspend fun <PE: ExposifyEntity> beforeInsertUpdate(
        entityContainer : EntityUpdateContainer<ENTITY, *, *, PE>,
        updateMode: UpdateMode)
    {
        val container =  entityContainer.castOrOperationsEx<EntityUpdateContainer<ENTITY, *, *, ExposifyEntity>>()
        bindingMap.values.forEach { it.beforeInsertedUpdate(container) }
        foreignIDClassDelegates.forEach { it.beforeInsertedUpdate(entityContainer) }
    }

    suspend fun <P_DTO: ModelDTO, PD: DataModel, FE: ExposifyEntity> afterInsertUpdate(
        entityContainer : EntityUpdateContainer<ENTITY, P_DTO, PD, FE>)
    {
        bindingMap.values.forEach { it.afterInsertedUpdate(entityContainer) }
        foreignIDClassDelegates.forEach { it.afterInsertedUpdate(entityContainer) }
    }

}

