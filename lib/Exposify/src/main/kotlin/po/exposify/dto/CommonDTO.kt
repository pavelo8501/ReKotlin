package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.binders.PropertyBinder
import po.exposify.binders.UpdateMode
import po.exposify.classes.components.DAOService2
import po.exposify.classes.components.RepositoryBase2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.components.DataModelContainer2
import po.exposify.dto.enums.CrudType
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.dto.models.CrudOperation
import po.exposify.dto.models.DTORegistryItem
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.models.DTOInitStatus


abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOClass2<DTO>
):ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    abstract val dataModel: DATA

    lateinit var daoService: DAOService2<DTO, ENTITY>
    lateinit var propertyBinder: PropertyBinder<DATA, ENTITY>
    override lateinit var dataContainer: DataModelContainer2<DTO, DATA>

    var onInitializationStatusChange : ((CommonDTO<DTO, DATA, ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }
    val crudOperation : CrudOperation = CrudOperation(CrudType.NONE, true)

    override val id : Long
        get(){return dataContainer.dataModel.id}


    private var _entityDAO : ENTITY? = null
    var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException(
            "Entity uninitialized",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    private var _regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>? = null
    private val regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>
        get(){
            return _regItem?:throw InitializationException("DtoClassRegistryItem uninitialized", InitErrorCodes.KEY_PARAM_UNINITIALIZED)
        }
    internal val registryItem: CommonDTORegistryItem<DTO, DATA, ENTITY> by lazy { regItem }
    internal var repositories =  MapBuilder<BindingKeyBase2,  RepositoryBase2<DTO, *>> ()

    init {
        val a = 10
    }

   suspend fun update(){
        crudOperation.setOperation(CrudType.UPDATE)
        dataContainer.trackedProperties.forEach {
            runCatching {
                repositories.map.getValue(it.value.bindingKey).update()
            }.onFailure {
                println("CommonDTO2 update ${it.message.toString()}")
            }
        }
        crudOperation.setOperation(CrudType.NONE)
    }

    fun compileDataModel():DATA{
        return dataModel
    }

    fun updateBinding(entity : ENTITY, updateMode: UpdateMode){
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        entityDAO = entity
        initStatus = DTOInitStatus.INITIALIZED
    }

   internal fun initialize(
       regItem: DTORegistryItem<DTO, DATA, ENTITY>,
       container : DataModelContainer2<DTO, DATA>,
       binder: PropertyBinder<DATA,ENTITY>,
       dao : DAOService2<DTO, ENTITY>){
       _regItem =  CommonDTORegistryItem(dtoClass, regItem.dataKClass, regItem.entityKClass, regItem.commonDTOKClass,this)
       propertyBinder = binder
       dataContainer = container
       daoService = dao
       dataContainer.attachBinder(propertyBinder)
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       selfRegistration(registryItem)
    }

    companion object{
        internal val dtoRegistry: MapBuilder<String, CommonDTORegistryItem<*,*,*>> = MapBuilder<String, CommonDTORegistryItem<*,*,*>>()
        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: LongEntity> selfRegistration(
            regItem :  CommonDTORegistryItem<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }
    }
}