package po.exposify.dto.components.relation_binder.models

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.components.relation_binder.BindingContainer
import po.exposify.dto.components.relation_binder.BindingKeyBase
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class EntityPropertyInfo<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    name: String,
    cardinality: Cardinality,
    isNullable: Boolean = false,
    bindingKey: BindingKeyBase,
    hostingContainer : BindingContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
) : PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(name, cardinality, isNullable, bindingKey, hostingContainer)
        where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel,
              ENTITY: ExposifyEntity, CHILD_DATA: DataModel, CHILD_ENTITY: ExposifyEntity
{

    fun getOwnEntitiesProperty(): KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>?{
        if(hostingContainer is MultipleChildContainer){
            return hostingContainer.ownEntitiesProperty
        }else{
            return null
        }
    }

    fun getOwnEntityProperty(): KProperty1<ENTITY, CHILD_ENTITY>?{
        if(hostingContainer is SingleChildContainer){
            return hostingContainer.ownEntityProperty
        }else{
            return null
        }
    }

}


class DataPropertyInfo<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    name: String,
    cardinality: Cardinality,
    isNullable: Boolean = false,
    bindingKey: BindingKeyBase,
    hostingContainer : BindingContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
) : PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(name, cardinality, isNullable, bindingKey, hostingContainer)
        where DTO : ModelDTO, CHILD_DTO: ModelDTO,
              DATA: DataModel, ENTITY: ExposifyEntity, CHILD_DATA : DataModel, CHILD_ENTITY : ExposifyEntity
{

    fun getOwnModelsProperty(): KProperty1<DATA, MutableList<CHILD_DATA>>?{
        if(hostingContainer is MultipleChildContainer){
            return hostingContainer.ownDataModelsProperty
        }else{
            return null
        }
    }

    fun getOwnModelProperty():  KMutableProperty1<DATA, CHILD_DATA?>?{
        if(hostingContainer is SingleChildContainer){
            return  hostingContainer.sourcePropertyWrapper.extract()
        }else{
            return null
        }
    }
 }

sealed class PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>(
    val name: String,
    val cardinality: Cardinality,
    val isNullable: Boolean = false,
    val bindingKey: BindingKeyBase,
    protected val hostingContainer : BindingContainer<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>
) where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntity,
        CHILD_DATA : DataModel, CHILD_ENTITY : ExposifyEntity
{

    var processed : Boolean = false
    var inBlueprint: KProperty1<DATA, *>?  = null

    fun getContainer():BindingContainer<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>{
        return hostingContainer
    }



}