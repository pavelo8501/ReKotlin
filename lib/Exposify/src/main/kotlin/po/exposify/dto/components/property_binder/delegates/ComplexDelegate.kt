package po.exposify.dto.components.property_binder.delegates

import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.getOrOperationsEx
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class ModelEntityIDDelegate<DATA, ENTITY, FOREIGN_ENTITY, FOREIGN_DTO>(
    dto: CommonDTO<ModelDTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    val entityProperty : KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
    val foreignDtoClass: DTOClass<FOREIGN_DTO>
): ComplexDelegate<DATA, ENTITY, FOREIGN_ENTITY, Long, Long>(dto, dataProperty)
    where DATA: DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase, FOREIGN_DTO: ModelDTO
{
    override fun updateEntity(entity: ENTITY) {
        entityProperty.set(entity, foreignDtoClass.getEntityModel<FOREIGN_ENTITY>()[getEffectiveValue()])
    }

    override fun updateDataModel(entity: ENTITY){
       val foreignEntity = entityProperty.get(entity)
       val id = foreignEntity.id.value
        dataProperty.set(dto.dataModel, id)
    }

    override fun getEffectiveValue(): Long{
        return dataProperty.get(dto.dataModel)
    }
}

class ModelEntityDTODelegate<DATA, ENTITY, FOREIGN_ENTITY, PARENT_DTO>(
    dto: CommonDTO<ModelDTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    val entityProperty : KMutableProperty1<ENTITY, ExposifyEntityBase>,
    val foreignDtoClass: DTOClass<PARENT_DTO>
): ComplexDelegate<DATA, ENTITY, ENTITY, Long, CommonDTO<PARENT_DTO, DataModel, ExposifyEntityBase>>(dto, dataProperty)

     where DATA: DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase, PARENT_DTO : ModelDTO
{

    override fun updateEntity(entity: ENTITY) {
        entityProperty.set(entity, foreignDtoClass.getEntityModel<FOREIGN_ENTITY>()[getEffectiveValue().id])
    }

    override fun updateDataModel(entity: ENTITY) {
        val foreignEntity = entityProperty.get(entity)
        val id = foreignEntity.id.value
        dataProperty.set(dto.dataModel, id)
    }


    override fun getEffectiveValue(): CommonDTO<PARENT_DTO, DataModel, ExposifyEntityBase>{
        val id = dataProperty.get(dto.dataModel)
        val dto = foreignDtoClass.lookupDTO(id)
        val foundDTO = dto.getOrOperationsEx("Dto for given id: $id can not be located")
        return foundDTO
    }
}


sealed class ComplexDelegate<DATA, ENTITY, FOREIGN_ENTITY, DV, RV>(
    protected val dto: CommonDTO<ModelDTO, DATA, ENTITY>,
    protected val dataProperty : KMutableProperty1<DATA, DV>,
): ReadOnlyProperty<ModelDTO, RV>
    where DATA: DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase
{

    init {
        setEntityUpdateCallback()
    }

    protected var ownEntity: ENTITY? = null

    abstract fun getEffectiveValue():RV
    abstract fun updateEntity(entity: ENTITY)
    abstract fun updateDataModel(entity: ENTITY)

    private fun setEntityUpdateCallback() {
        dto.daoService.apply {
            setBeforeInsertedHook { entity -> updateEntity(entity) }
            setAfterInsertedHook { entity ->
                ownEntity = entity
                updateDataModel(entity)
            }
        }
    }

    var dTOProperty: KProperty<*>? = null
    var dTOPropertyName: String = ""
    override fun getValue(thisRef: ModelDTO, property: KProperty<*>):RV{
        dTOPropertyName = property.name
        dTOProperty = property
        return getEffectiveValue()
    }

}
