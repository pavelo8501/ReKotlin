package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.constructors.DTOBlueprint
import po.db.data_service.constructors.DataModelBlueprint
import po.db.data_service.constructors.EntityBlueprint
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KClass

class Factory<DATA, ENTITY>(
   parent: DTOClass<DATA,ENTITY>,
 val  dataModelClass: KClass<DATA>,
 val  daoEntityClass : KClass<ENTITY>
)where DATA: DataModel,   ENTITY: LongEntity  {
    companion object : ConstructorBuilder()


    private var dataBlueprint = DataModelBlueprint<DATA>(dataModelClass)
    private var entityBlueprint = EntityBlueprint<ENTITY>(daoEntityClass)
    private val daoBlueprint = DTOBlueprint<DATA,ENTITY>(dataModelClass, daoEntityClass)


    private var dataModelConstructor : (() -> DATA)? = null
    fun setDataModelConstructor(dataModelConstructor : (() -> DATA)){
        this.dataModelConstructor = dataModelConstructor
    }

    fun createDataModel():DATA{
        try{
            dataModelConstructor?.let { return  it.invoke() }

            dataBlueprint.let {blueprint->
                return  getArgsForConstructor(blueprint).let {argMap->
                    blueprint.getConstructor().let { construct->
                      val data =  construct.callBy(argMap)
                      data
                    }
                }
            }
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }

//    fun copyAsChild(thisClassName: String): EntityDTO<DATA,ENTITY> {
//        return object : EntityDTO<DATA, ENTITY>(this, this.childDataSource) {
//            override val dataModel: DataModel = this.injectedDataModel
//            override val className: String = thisClassName
//        }
//    }

    fun createDtoEntity(dataModel : DataModel? = null): EntityDTO<DATA,*>? {
        val model = dataModel?: createDataModel()
        try {
            daoBlueprint.let { blueprint ->
                return getArgsForConstructor(blueprint) {
                    when (it) {
                        "dataModel" -> {
                            model
                        }

                        else -> {
                            null
                        }
                    }
                }.let { blueprint.getEffectiveConstructor().callBy(it) as EntityDTO<DATA,*> }
            }
            return null
        }catch (ex: Exception) {
            throw OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
    }
}
