package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.constructors.ClassBlueprint
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.CommonDTO2
import kotlin.reflect.KClass

class DTOFactory<ENTITY>(
)where ENTITY: LongEntity {

    companion object : ConstructorBuilder()

    val factoryName: String = ""

    private var dtoBlueprint: ClassBlueprint? = null
    private var dataBlueprint: ClassBlueprint? = null

    var dtoModelClass: KClass<out DTOEntity>? = null
        set(value){
            if(value != null){
                field = value
                dtoBlueprint = getConstructorBlueprint(value)
            }
        }

    var dataModelClass: KClass<out DataModel>? = null
        set(value){
            if(value != null){
                field = value
                dataBlueprint = getConstructorBlueprint(value)
            }
        }

    private var dataModelConstructor : (() -> DataModel)? = null

    fun setDataModelConstructor(dataModelConstructor : (() -> DataModel)){
        this.dataModelConstructor = dataModelConstructor
    }

    fun constructDtoEntity(dataModel : DataModel? = null): CommonDTO2? {
        val model = dataModel?: constructDataModel()
        try {
            dtoBlueprint?.let { blueprint ->
                return getArgsForConstructor(blueprint) {
                    when (it) {
                        "dataModel" -> {
                            model
                        }

                        else -> {
                            null
                        }
                    }
                }.let { blueprint.getEffectiveConstructor().callBy(it) as CommonDTO2 }
            }
            return null
        }catch (ex: Exception) {
            throw OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
    }

    fun constructDataModel():DataModel?{
        try{
            if(dataModelConstructor != null) {
                 return   dataModelConstructor?.invoke()
            }
            dataBlueprint?.let {blueprint->
               return  getArgsForConstructor(blueprint).let {blueprint.getEffectiveConstructor().callBy(it) as DataModel}
            }
            return null
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }

}