package po.exposify.dto.components


import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.throwInit
import po.exposify.exceptions.throwOperations


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
            throwOperations("Property value type mismatch", ExceptionCode.REFLECTION_ERROR)
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