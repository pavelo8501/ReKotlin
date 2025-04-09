package po.exposify.dto

import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.binders.PropertyBinder
import po.exposify.binders.UpdateMode
import po.exposify.dto.components.DAOService
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.classes.DTOClass
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.models.DTOInitStatus
import po.lognotify.extensions.getOrThrowDefault


abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOClass<DTO>
):ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: ExposifyEntityBase {

    var personalName : String = "unset"
    abstract val dataModel: DATA

    lateinit var daoService: DAOService<DTO, DATA, ENTITY>
    lateinit var propertyBinderSource: PropertyBinder<DATA, ENTITY>
    val propertyBinder : PropertyBinder<DATA, ENTITY>
        get() {
            return propertyBinderSource
        }

    override lateinit var dataContainer: DataModelContainer<DTO, DATA>

    var onInitializationStatusChange : ((CommonDTO<DTO, DATA, ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }
    override var id : Long = 0
        get(){return dataContainer.dataModel.id}

    val entityDAO : ENTITY
        get(){return daoService.getLastEntity()}

    private var _regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>? = null
    private val regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>
        get(){
            return _regItem?:throw InitException("DtoClassRegistryItem uninitialized", ExceptionCode.UNDEFINED)
        }
    internal val registryItem: CommonDTORegistryItem<DTO, DATA, ENTITY> by lazy { regItem }
    internal var repositories =  MapBuilder<BindingKeyBase2,  RepositoryBase<DTO, DATA, ENTITY, ModelDTO>> ()

    internal fun getRepository(key: BindingKeyBase2):RepositoryBase<DTO, DATA, ENTITY, ModelDTO>{
       return repositories.map[key].getOrThrowDefault("Child repository not found @ $personalName")
    }

    fun updateBinding(entity : ENTITY, updateMode: UpdateMode){
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        dataContainer.setDataModelId(entity.id.value)
        initStatus = DTOInitStatus.INITIALIZED
    }

   internal fun initialize(
       regItem: DTORegistryItem<DTO, DATA, ENTITY>,
       container : DataModelContainer<DTO, DATA>,
       binder: PropertyBinder<DATA,ENTITY>,
       dao : DAOService<DTO, DATA,  ENTITY>)
   {
       _regItem =  CommonDTORegistryItem(dtoClass, regItem.dataKClass, regItem.entityKClass, regItem.commonDTOKClass,this)
       propertyBinderSource = binder
       dataContainer = container
       daoService = dao
       dataContainer.attachBinder(propertyBinder)
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       personalName = "${regItem.commonDTOKClass.simpleName.toString()}[CommonDTO]"
       selfRegistration(registryItem)
    }

    companion object{
        internal val dtoRegistry: MapBuilder<String, CommonDTORegistryItem<*,*,*>> = MapBuilder<String, CommonDTORegistryItem<*,*,*>>()
        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: ExposifyEntityBase> selfRegistration(
            regItem :  CommonDTORegistryItem<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }
    }
}