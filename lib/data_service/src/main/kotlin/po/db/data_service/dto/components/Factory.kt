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
   val parent: DTOClass<DATA,ENTITY>,
   val entityDTOClass : KClass<out EntityDTO<DATA, ENTITY>>
)where DATA: DataModel,   ENTITY: LongEntity   {
    companion object : ConstructorBuilder()

    private var _dataModelClass: KClass<DATA>? = null
    private val dataModelClass: KClass<DATA>
        get(){return _dataModelClass?: throw OperationsException("DataModel Class uninitialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }

    private var _daoEntityClass: KClass<ENTITY>? = null
    private val daoEntityClass: KClass<ENTITY>
        get(){return _daoEntityClass?: throw OperationsException("DataModel Class uninitialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }

    private var dataBlueprint : DataModelBlueprint<DATA>? = null
    private lateinit var entityBlueprint : EntityBlueprint<ENTITY>
    private val daoBlueprint = DTOBlueprint(entityDTOClass).also { it.initialize(Companion) }

    private var dataModelConstructor : (() -> DATA)? = null

    /**
     * Initializes the blueprints for DataModel, Entity, and DTO based on the provided classes.
     *  @input dataClazz: KClass<DATA>
     *  @input entityClass : KClass<ENTITY>
     */
    fun initializeBlueprints(
        dataClazz : KClass<DATA>,
        entityClass : KClass<ENTITY>,
    ){
        _dataModelClass = dataClazz
        _daoEntityClass = entityClass

        dataBlueprint =   DataModelBlueprint(dataModelClass).also { it.initialize(Companion) }
        entityBlueprint = EntityBlueprint(daoEntityClass).also { it.initialize(Companion) }
    }

    fun setDataModelConstructor(dataModelConstructor : (() -> DATA)){
        this.dataModelConstructor = dataModelConstructor
    }

    /**
     * Create new instance of DatModel injectable to the specific EntityDTO<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    fun createDataModel(constructFn : (() -> DATA)? = null):DATA{
        try{
            constructFn?.let { return  it.invoke() }

            dataBlueprint?.let {blueprint->
                return  getArgsForConstructor(blueprint).let {argMap->
                    blueprint.getConstructor().let { construct->
                      val data =  construct.callBy(argMap)
                      data
                    }
                }
            }?:run {
                TODO("Extract DATA blueprint from entityDTOClass as a reserve fallback")
            }
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }
    /**
     * Create new instance of  EntityDTO
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return EntityDTO<DATA, ENTITY> or null
     * */
    fun createEntityDto(dataModel : DATA? = null): EntityDTO<DATA, ENTITY>? {
        val model = dataModel?: createDataModel()
        try {
            daoBlueprint.let { blueprint ->
                val constructor =  blueprint.getConstructor()
                blueprint.getArgsForConstructor {paramName->
                    when (paramName) {
                        "dataModel" -> {
                            model
                        }
                        else -> {
                            null
                        }
                    }
                }.let {
                   println("Argument map used for creation $it")
                 return  constructor.callBy(it)
                }
            }
            return null
        }catch (ex: Exception) {
            throw OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
    }
}
