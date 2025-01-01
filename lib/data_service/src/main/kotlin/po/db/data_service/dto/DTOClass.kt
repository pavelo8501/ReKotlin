package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.models.DaoFactory

abstract class DTOClass<ENTITY> where ENTITY : LongEntity  {

    companion object : ConstructorBuilder()

    var initialized: Boolean = false
    var className : String = "Undefined"
    var conf = DTOConfig<ENTITY>(this)
        private set

    var onDtoInitializationCallback: ((DTOClass<*>) -> ClassBlueprintContainer)? = null
        private set
    private var _blueprints : ClassBlueprintContainer? = null
    private val blueprints : ClassBlueprintContainer
        get(){
            return  _blueprints?: throw InitializationException("blueprints for Class $className no set", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    val daoModel: LongEntityClass<ENTITY>
        get(){
            return  conf.daoModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    private val dtoContainer = mutableListOf<CommonDTO>()

    protected abstract fun setup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun getAssociatedTables():List<IdTable<Long>>{
       val result = mutableListOf<IdTable<Long>>()
       result.add(this.daoModel.table)
       result.addAll(conf.relationBinder.getDependantTables())
       return result
    }

    fun setConfiguration(className: String, config: DTOConfig<ENTITY>) {
        conf = config
        this.className = className
    }

    fun initialization(onDtoInitialization: (DTOClass<*>) -> ClassBlueprintContainer) {
        onDtoInitializationCallback = onDtoInitialization
        setup()
        onDtoInitialization(this).let {
            _blueprints = it
        }
        initialized = true
    }

    private fun constructDtoEntity(dataModel : DataModel):CommonDTO{
        val dtoEntity = try {
            val params = blueprints.dtoModel.constructorParams
            val args = getArgsForConstructor(blueprints.dtoModel) {
                when (it) {
                    "dataModel" -> {
                        dataModel
                    }
                    else -> {
                        null
                    }
                }
            }
            val dtoEntity = blueprints.dtoModel.getEffectiveConstructor().callBy(args) as CommonDTO
            dtoEntity.initialize(conf.propertyBinder)
            dtoEntity
        } catch (ex: Exception) {
            throw OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
        return dtoEntity
    }

    private fun constructDataModel():DataModel{
        try{
            val model = conf.dataModelConstructor?.invoke().let { model ->
                val args = getArgsForConstructor(blueprints.dataModel)
                model ?: blueprints.dataModel.getEffectiveConstructor().callBy(args) as DataModel
            }
            return model
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }

    /**
     * Create new CommonDTO entity from DataModel provided
     * @input dataModel: DataModel
     * @return CommonDTO
     * */
    fun create(dataModel: DataModel, daoFactory: DaoFactory) : CommonDTO {
        val newDTO = create(dataModel)
        daoFactory.new(this)
        conf.relationBinder.getBindingList().forEach { binding->
            if(newDTO.childDataSource!= null){
                newDTO.childDataSource.forEach { dataModel->
                    val childDTO = binding.createChild(newDTO, dataModel, daoFactory)
                    newDTO.childDTOs.add(childDTO)
                }
            }
        }
        return  newDTO
    }

    fun create(dataModel: DataModel) : CommonDTO {
        val newDTO = constructDtoEntity(dataModel)
        dtoContainer.add(newDTO)
        return  newDTO
    }

    fun create(daoEntity: ENTITY) : CommonDTO {
        val dataModel = constructDataModel()
        val newDTO = constructDtoEntity(dataModel)
        newDTO.updateDTO(daoEntity, this)
        conf.relationBinder.getBindingList().forEach { binding ->
            binding.loadChild(daoEntity).let {
                newDTO.childDTOs.addAll(it)
            }
        }
        dtoContainer.add(newDTO)
        return newDTO
    }

    inline fun <reified DTO, reified DATA> dtoSettings(
        daoModel: LongEntityClass<ENTITY>,
        block: DTOConfig<ENTITY>.() -> Unit
    ) where  DTO : DTOEntity, DATA : DataModel {
        val rootDtoModelClass = DTO::class
    val newConf = DTOConfig<ENTITY>(this)
        newConf.setClassData(rootDtoModelClass, DATA::class, daoModel)
        setConfiguration(rootDtoModelClass.simpleName!!, newConf)
        newConf.block()
    }
}

