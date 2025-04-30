package po.exposify.dto.components

import com.sun.jdi.Value
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.components.relation_binder.models.DataPropertyInfo
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


class DataModelContainer<DTO : ModelDTO, DATA: DataModel>(
    internal val dataModel: DATA,
    val dataBlueprint: ClassBlueprint<DATA>,
    private var binder: PropertyBinder<DATA, *>? = null,
): DataModel {

    override var id: Long = dataModel.id

    val trackedProperties: MutableMap<String, DataPropertyInfo<DTO, DATA, ExposifyEntity, ModelDTO>> = mutableMapOf()

    operator fun <V: Any?>  getValue(name: String,  property: KProperty<*>): V {
        return  dataBlueprint.getProperty(dataModel,  name)
    }

    operator fun <V>  setValue(name: String,  property: KProperty<*>, value: V){
        dataBlueprint.setProperty(dataModel, name,  value)
    }

    fun binderUpdatedProperty(name : String, type : PropertyType, updateMode : UpdateMode){
       // println("BinderUpdatedProperty callback triggered by $name")
    }

    fun setDataModelId(id: Long){
        dataModel.id = id
    }

    fun setTrackedProperties(list: List<DataPropertyInfo<DTO, DATA, ExposifyEntity, ModelDTO>>){
        list.forEach {propertyInfo->
            trackedProperties[propertyInfo.name] = propertyInfo
        }
    }

    fun attachBinder(propertyBinder: PropertyBinder<DATA, *>){
        binder = propertyBinder
        propertyBinder.onPropertyUpdate
        propertyBinder.syncedSerializedPropertyList.forEach {
            it.dataProperty
        }
    }

    fun extractChildModels(
        forPropertyInfo : DataPropertyInfo<DTO, DATA, ExposifyEntity, ModelDTO>): List<DataModel>
    {
        if(forPropertyInfo.cardinality == Cardinality.ONE_TO_MANY){
            val property = forPropertyInfo.getOwnModelsProperty()
            if(property != null) {
                return property.get(dataModel).toList()
            }else{
                throw OperationsException(
                    "Property for name ${forPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_MANY",
                    ExceptionCode.BINDING_PROPERTY_MISSING)
            }
        }

        if(forPropertyInfo.cardinality == Cardinality.ONE_TO_ONE){
            val property = forPropertyInfo.getOwnModelProperty()
            if(property != null) {
                val dataModel = property.get(dataModel)
                if(dataModel != null){
                    return  listOf<DataModel>(dataModel)
                }
            }else{
                throw OperationsException(
                    "Property for name ${forPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_ONE",
                    ExceptionCode.BINDING_PROPERTY_MISSING)
            }
        }
        return emptyList()
    }
}