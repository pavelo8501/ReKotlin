package po.exposify.dto.components


import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode


class DataModelContainer<DTO : ModelDTO, DATA: DataModel>(
    internal var dataModel: DATA,
    val dataBlueprint: ClassBlueprint<DATA>,
): DataModel {

    override var id: Long = dataModel.id

    var onDataModelUpdated : ((DATA)-> Unit)? = null

    fun extractValue(model:DATA, propertyName: String): Any?{
       return dataBlueprint.getValue(model, propertyName)
    }

    fun<V> extractTypedValue(model:DATA, propertyName: String): V{
        val value = dataBlueprint.getValue(model, propertyName)
        @Suppress("UNCHECKED_CAST")
        val isValue = value as? V
        if(isValue != null){
            return isValue
        }else{
            throw OperationsException("Property value type mismatch", ExceptionCode.REFLECTION_ERROR)
        }
    }

    fun updateDataModel(data:DATA){
        dataModel = data
        onDataModelUpdated?.invoke(dataModel)
    }

    fun setDataModelId(id: Long){
        dataModel.id = id
    }

}