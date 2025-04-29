package po.exposify.dto.components.property_binder.delegates

import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.misc.types.castOrThrow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class ModelEntityIDDelegate<DATA, ENTITY, FOREIGN_ENTITY, FOREIGN_DTO>(
    dto: CommonDTO<ModelDTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    entityProperty : KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
    val foreignDtoClass: DTOClass<FOREIGN_DTO>
): ComplexDelegate<DATA, ENTITY, FOREIGN_ENTITY, Long, Long>(dto, dataProperty, entityProperty)
    where DATA: DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase, FOREIGN_DTO: ModelDTO
{
    override fun updateByEntity(
        entity: ENTITY,
        foreignDTO: CommonDTO<ModelDTO, DataModel, ExposifyEntityBase>?
    ) {
        val foreign = foreignDtoClass.getEntityModel<FOREIGN_ENTITY>()[getEffectiveValue()]
        entityProperty.set(entity, foreign)
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

class ModelEntityDTODelegate<DATA, ENTITY, FOREIGN_ENTITY,  FOREIGN_DTO>(
    dto: CommonDTO<ModelDTO, DATA, ENTITY>,
    dataProperty : KMutableProperty1<DATA, Long>,
    entityProperty : KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
    val foreignDtoClass: DTOClass<FOREIGN_DTO>
): ComplexDelegate<DATA, ENTITY, FOREIGN_ENTITY, Long, CommonDTO<FOREIGN_DTO, DataModel, ExposifyEntityBase>>(dto, dataProperty, entityProperty)

     where DATA: DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase, FOREIGN_DTO : ModelDTO
{

    private var acquiredForeignDTO : CommonDTO<FOREIGN_DTO, DataModel, ExposifyEntityBase>? = null
    override fun getEffectiveValue(): CommonDTO<FOREIGN_DTO, DataModel, ExposifyEntityBase>{
         return acquiredForeignDTO?:run {
             val id = dataProperty.get(dto.dataModel)
             val dto = foreignDtoClass.lookupDTO(id, foreignDtoClass)
             dto.getOrOperationsEx("Dto for given id: $id can not be located")
         }
    }

    override fun updateByEntity(
        entity: ENTITY,
        foreignDTO: CommonDTO<ModelDTO, DataModel, ExposifyEntityBase>?
    ) {
        if(foreignDTO != null){
            acquiredForeignDTO =  foreignDTO.castOrThrow<CommonDTO<FOREIGN_DTO, DataModel, ExposifyEntityBase>, InitException>()
            dataProperty.set(dto.dataModel, acquiredForeignDTO!!.id)
        }else{
            throw InitException("ForeignDTO should have been provided", ExceptionCode.ABNORMAL_STATE)
        }
    }

    override fun updateDataModel(entity: ENTITY) {
        val foreignEntity = entityProperty.get(entity)
        val id = foreignEntity.id.value
        dataProperty.set(dto.dataModel, id)
    }
}


sealed class ComplexDelegate<DATA, ENTITY, FOREIGN_ENTITY, DV, RV>(
    protected val dto: CommonDTO<ModelDTO, DATA, ENTITY>,
    protected val dataProperty : KMutableProperty1<DATA, DV>,
    protected val entityProperty : KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
): ReadOnlyProperty<ModelDTO, RV>
    where DATA: DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase
{

    protected val entityPropertyName : String  get() = entityProperty.name

    init {
        setEntityUpdateCallback()
    }

    protected var ownEntity: ENTITY? = null

    abstract fun getEffectiveValue():RV
    abstract fun updateByEntity(entity: ENTITY, foreignDTO: CommonDTO<ModelDTO, DataModel, ExposifyEntityBase>?)
    abstract fun updateDataModel(entity: ENTITY)

    private fun setEntityUpdateCallback() {
        dto.daoService.apply {
            setBeforeInsertedHook(entityPropertyName) {updateMode, entity, foreignDTO ->
                val  dtoName = dto.personalName
                if(updateMode == UpdateMode.MODEL_TO_ENTITY || updateMode == UpdateMode.MODEL_TO_ENTITY_FORCED){
                    if(foreignDTO != null){
                        updateByEntity(entity, foreignDTO)
                    }else{
                        updateByEntity(entity, null)
                    }
                }else{

                }
            }
            setAfterInsertedHook(entityPropertyName) {updateMode, entity ->
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
