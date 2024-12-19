package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.components.DTOConfigV2
import po.db.data_service.dto.interfaces.DTOModelClass
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.CommonDTOV2
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceContextV2


abstract class DTOClassV2(){

    companion object : ConstructorBuilder()

    var initialized: Boolean = false
    var className : String = ""
    var configuration : DTOConfigV2?  = null
    private var blueprints : ClassBlueprintContainer? = null

    val daoModel : LongEntityClass<LongEntity>
        get(){
            return  configuration?.daoModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    protected abstract fun setup()

    fun setConfiguration(className : String, config : DTOConfigV2){
        configuration = config
        this.className = className
    }

    fun initialization(onDtoInitialized: (DTOClassV2)-> ClassBlueprintContainer){
        setup()
        onDtoInitialized(this).let {
            blueprints = it
        }
        initialized = true
    }

    fun create(daoEntity : LongEntity):CommonDTOV2{
        val dataModel = try {
            val model = configuration?.dataModelConstructor?.invoke().let { model ->
                model?: blueprints?.data?.getEffectiveConstructor()?.callBy(blueprints!!.data.constructorParams)
            }
            model
        }catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }

        val dtoEntity = try {
            val params = blueprints?.model?.constructorParams
            val args = getArgsForConstructor(blueprints!!.model){
                when(it){
                    "dataModel"->{
                        dataModel
                    }
                    else->{null}
                }
            }
            val  dtoEntity = blueprints!!.model.getEffectiveConstructor().callBy(args) as CommonDTOV2
            dtoEntity
        }catch (ex:Exception){
            throw  OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
        dtoEntity.setEntityDAO(daoEntity,this)
        return dtoEntity
    }

    inline fun  <reified DTO, reified DATA> dtoSettings(daoModel : LongEntityClass<LongEntity>, block: DTOConfigV2.() -> Unit)
    where  DTO : DTOModelV2, DATA : DataModel {
        val config = DTOConfigV2()
        config.block()
        val rootDtoModelClass = DTO::class
        config.setClassData(rootDtoModelClass, DATA::class, daoModel)
        setConfiguration(rootDtoModelClass.qualifiedName!!,config)
    }

}

