package po.exposify.binders.relationship.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.binders.relationship.MultipleChildContainer2
import po.exposify.binders.relationship.SingleChildContainer2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class EntityPropertyInfo<DTO, DATA, ENTITY, CHILD_DTO>(
    name: String,
    cardinality: Cardinality,
    isNullable: Boolean = false,
    bindingKey: BindingKeyBase2,
    hostingContainer : BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>
) : PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO>(name, cardinality, isNullable, bindingKey, hostingContainer)
        where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntityBase
{

    fun getOwnEntitiesProperty(): KProperty1<ENTITY, Iterable<ExposifyEntityBase>>?{
        if(hostingContainer is MultipleChildContainer2){
            return hostingContainer.ownEntitiesProperty
        }else{
            return null
        }
    }

    fun getOwnEntityProperty(): KProperty1<ENTITY, ExposifyEntityBase>?{
        if(hostingContainer is SingleChildContainer2){
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
    bindingKey: BindingKeyBase2,
    hostingContainer : BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>
) : PropertyInfoBase<DTO, DATA, ENTITY, CHILD_DTO>(name, cardinality, isNullable, bindingKey, hostingContainer)
        where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntityBase
{

    fun getOwnModelsProperty(): KProperty1<DATA, Iterable<DataModel>>?{
        if(hostingContainer is MultipleChildContainer2){
            return hostingContainer.ownDataModelsProperty
        }else{
            return null
        }
    }

    fun getOwnModelProperty():  KMutableProperty1<DATA, DataModel?>?{
        if(hostingContainer is SingleChildContainer2){
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
    val bindingKey: BindingKeyBase2,
    protected val hostingContainer : BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>
) where DTO : ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY: ExposifyEntityBase{

    var processed : Boolean = false
    var inBlueprint: KProperty1<DATA, *>?  = null

    fun getContainer():BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>{
        return hostingContainer
    }



}