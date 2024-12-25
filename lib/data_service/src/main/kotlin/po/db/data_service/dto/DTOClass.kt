package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.UpdateMode
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceContextV2

abstract class DTOClass<ENTITY> where ENTITY : LongEntity  {

    companion object : ConstructorBuilder()

    var initialized: Boolean = false
    var className : String = "Undefined"
    var conf = DTOConfig(this)
        private set

    var onDtoInitializationCallback: ((DTOClass<*>) -> ClassBlueprintContainer)? = null
        private set
    private var _blueprints : ClassBlueprintContainer? = null
    private val blueprints : ClassBlueprintContainer
        get(){
            return  _blueprints?: throw InitializationException("blueprints for Class $className no set", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    private var _daoEntity: LongEntity? = null

    val daoModel: LongEntityClass<LongEntity>
        get(){
            return  conf.daoModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    fun daoEntity(id:Long):LongEntity{
        return this._daoEntity?: throw OperationsException("Reading daoEntity while undefined for $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
    }

    private val dtoContainer = mutableListOf<CommonDTO>()

    protected abstract fun setup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
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

    /**
     * Create new CommonDTO entity from DataModel provided
     * @input dataModel: DataModel
     * @return CommonDTO
     * */
    fun create(dataModel: DataModel) : CommonDTO {
        val newDTO = constructDtoEntity(dataModel)
        dtoContainer.add(newDTO)
        return  newDTO
    }

    fun create(daoEntity: LongEntity) : CommonDTO {
        val dataModel = try {
            val model = conf.dataModelConstructor?.invoke().let { model ->
                val params = blueprints.dataModel.constructorParams
                val args = getArgsForConstructor(blueprints.dataModel)
                model ?: blueprints.dataModel.getEffectiveConstructor().callBy(args)
            }
            model
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }

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
            dtoEntity
        } catch (ex: Exception) {
            throw OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
        dtoEntity.setEntityDAO(daoEntity, this)
        dtoContainer.add(dtoEntity)
        _daoEntity = daoEntity

        conf.relationBinder.loadChildren((daoEntity) , "DepartmentDTOV2")
        return dtoEntity
    }

    fun update(update: DataModel, from: LongEntity) {
        conf.propertyBinder?.updateProperties(update, from, UpdateMode.ENTITY_TO_MODEL)
    }

    fun update(update: LongEntity, from: DataModel) {
        conf.propertyBinder?.updateProperties(from, update, UpdateMode.MODEL_TO_ENTITY)
    }

    inline fun <reified DTO, reified DATA> dtoSettings(
        daoModel: LongEntityClass<LongEntity>,
        block: DTOConfig<ENTITY>.() -> Unit
    ) where  DTO : DTOModelV2, DATA : DataModel {
        val rootDtoModelClass = DTO::class
        val newConf = DTOConfig(this)
        newConf.setClassData(rootDtoModelClass, DATA::class, daoModel)
        setConfiguration(rootDtoModelClass.simpleName!!, newConf)
        newConf.block()
    }
}

