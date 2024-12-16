package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.constructors.ClassBlueprint
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.CanNotify
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.controls.Notificator
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceContext


enum class ContextState{
    UNINITIALIZED,
    BEFORE_INITIALIZATION,
    INITIALIZED,
    INITIALIZATION_FAILURE
}



class DTOComponents<DATA_MODEL, ENTITY>(
    block : (DTOComponents<DATA_MODEL, ENTITY>.()->Unit)? = null
): CanNotify where   DATA_MODEL : DataModel, ENTITY : LongEntity {

    companion object : ConstructorBuilder()
    val configuration = DTOConfig<DATA_MODEL, ENTITY>()
    //EX inner
    override val name = "DtoComponents"
    override var notificator = Notificator(this)

    init {

    }

    private var  _dtoModelBlueprint : ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>? = null
    private val dtoModelBlueprint : ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>
        get(){
            return _dtoModelBlueprint?: throw InitializationException("dtoModelBlueprint requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    private var  _dataModelBlueprint : ClassBlueprint<DATA_MODEL>? = null
    private val dataModelBlueprint : ClassBlueprint<DATA_MODEL>
        get(){
            return _dataModelBlueprint?: throw InitializationException("dataModelBlueprint requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }
    fun setBlueprints(dtoBluePrint : ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>, dataModelBlueprint : ClassBlueprint<DATA_MODEL>?){
        _dtoModelBlueprint = dtoBluePrint
        _dataModelBlueprint = dataModelBlueprint
    }

    var serviceContext: ServiceContext<DATA_MODEL, ENTITY>? = null
        get() {return field }
        set(value){
            field = value
        }

    fun create(daoEntity : ENTITY, dtoModel: DTOClass<DATA_MODEL, ENTITY>): CommonDTO<DATA_MODEL, ENTITY> {
        val dataModel = try {
            val model = configuration.dataModelConstructor?.invoke().let { model ->
                model?: dataModelBlueprint.getEffectiveConstructor().callBy(dataModelBlueprint.constructorParams)
            }
            model
        }catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }

        val dtoEntity = try {
            val params = dtoModelBlueprint.constructorParams
            val args =  getArgsForConstructor(dtoModelBlueprint){
                when(it){
                    "dataModel"->{
                        dataModel
                    }
                    else->{null}
                }
            }
            dtoModelBlueprint.getEffectiveConstructor().callBy(args)
        }catch (ex:Exception){
            throw  OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
        dtoEntity.setEntityDAO(daoEntity,dtoModel)
        return dtoEntity
    }
}
