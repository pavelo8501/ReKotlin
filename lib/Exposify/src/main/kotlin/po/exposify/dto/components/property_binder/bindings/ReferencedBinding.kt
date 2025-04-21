package po.exposify.dto.components.property_binder.bindings

import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.extensions.pickById
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import kotlin.reflect.KMutableProperty1

class ReferencedBinding<DATA, ENTITY>(
    override val dataProperty: KMutableProperty1<DATA, Long>,
    override val referencedProperty: KMutableProperty1<ENTITY, out ExposifyEntityBase>,
    val dtoClass: DTOClass<out ModelDTO>,
): PropertyBindingOption<DATA, ENTITY, Long> where DATA: DataModel, ENTITY: ExposifyEntityBase
{
    override val propertyType: PropertyType = PropertyType.REFERENCED

    val castedEntityProperty = referencedProperty.castOrOperationsEx<KMutableProperty1<ENTITY,ExposifyEntityBase>>(
        "Cast to KMutableProperty1<ENTITY,ExposifyEntityBase> failed",
        ExceptionCode.CAST_FAILURE)


    override var onDataUpdatedCallback: ((PropertyBindingOption<DATA, ENTITY, Long>) -> Unit)? = null


    override fun onPropertyUpdated(callback: (String, PropertyType, UpdateMode) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updated(name: String, type: PropertyType, updateMode: UpdateMode){

    }

    private fun updateEntityToModel(entity:ENTITY, data :DATA, forced: Boolean){
        val referencedEntity = castedEntityProperty.get(entity)
        dataProperty.set(data, referencedEntity.id.value)
    }

    private suspend fun updateModelToEntity(data:DATA, entity: ENTITY, forced: Boolean){
        val castedDtoClass = dtoClass.safeCast<DTOClass<ModelDTO>>().getOrOperationsEx(
            "Cast to DTOClass<ModelDTO> failed",  ExceptionCode.CAST_FAILURE)

        val referencedId = dataProperty.get(data)
        if(!dtoClass.initialized){
            dtoClass.initialization()
        }
        val dto = castedDtoClass.pickById<DataModel>(referencedId).getDTO()
        castedEntityProperty.set(entity, dto.entityDAO)

    }

    suspend fun update(data: DATA, entity : ENTITY, mode: UpdateMode){
        when(mode){
            UpdateMode.ENTITY_TO_MODEL ->  updateEntityToModel(entity, data, false)
            UpdateMode.ENTITY_TO_MODEL_FORCED -> updateEntityToModel(entity, data, true)
            UpdateMode.MODEL_TO_ENTITY -> updateModelToEntity(data, entity,false)
            UpdateMode.MODEL_TO_ENTITY_FORCED -> updateModelToEntity(data, entity,true)
        }
    }
}