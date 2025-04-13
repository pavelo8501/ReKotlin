package po.exposify.dto.components.relation_binder.models

import po.exposify.dto.enums.Cardinality
import po.exposify.dto.components.relation_binder.BindingContainer
import po.exposify.dto.components.relation_binder.BindingKeyBase
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class EntityPropertyInfo<DTO, DATA, ENTITY, CHILD_DTO>(
    name: String,
    cardinality: Cardinality,
    isNullable: Boolean = false,
    bindingKey: BindingKeyBase,
    hostingContainer : BindingContainer<DTO, DATA, ENTITY, CHILD_DTO>
) : PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO>(name, cardinality, isNullable, bindingKey, hostingContainer)
        where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntityBase
{

    fun getOwnEntitiesProperty(): KProperty1<ENTITY, Iterable<ExposifyEntityBase>>?{
        if(hostingContainer is MultipleChildContainer){
            return hostingContainer.ownEntitiesProperty
        }else{
            return null
        }
    }

    fun getOwnEntityProperty(): KProperty1<ENTITY, ExposifyEntityBase>?{
        if(hostingContainer is SingleChildContainer){
            return hostingContainer.ownEntityProperty
        }else{
            return null
        }
    }

}


class DataPropertyInfo<DTO, DATA, ENTITY, CHILD_DTO>(
    name: String,
    cardinality: Cardinality,
    isNullable: Boolean = false,
    bindingKey: BindingKeyBase,
    hostingContainer : BindingContainer<DTO, DATA, ENTITY, CHILD_DTO>
) : PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO>(name, cardinality, isNullable, bindingKey, hostingContainer)
        where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntityBase
{

    fun getOwnModelsProperty(): KProperty1<DATA, Iterable<DataModel>>?{
        if(hostingContainer is MultipleChildContainer){
            return hostingContainer.ownDataModelsProperty
        }else{
            return null
        }
    }

    fun getOwnModelProperty():  KMutableProperty1<DATA, DataModel?>?{
        if(hostingContainer is SingleChildContainer){
            return  hostingContainer.sourcePropertyWrapper.extract()
        }else{
            return null
        }
    }
 }

sealed class PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO>(
    val name: String,
    val cardinality: Cardinality,
    val isNullable: Boolean = false,
    val bindingKey: BindingKeyBase,
    protected val hostingContainer : BindingContainer<DTO, DATA, ENTITY, CHILD_DTO>
) where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntityBase{

    var processed : Boolean = false
    var inBlueprint: KProperty1<DATA, *>?  = null

    fun getContainer():BindingContainer<DTO, DATA, ENTITY, CHILD_DTO>{
        return hostingContainer
    }



}