package po.exposify.dto

import po.exposify.dto.components.relation_binder.BindingKeyBase
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.DAOService
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.classes.DTOClass
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.dto.models.DTORegistryItem
import po.exposify.extensions.getOrOperationsEx
import po.misc.types.castOrThrow

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOClass<DTO>
): ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: ExposifyEntityBase {

    override var personalName : String = "unset"
    abstract override val dataModel: DATA

    override val daoService: DAOService<DTO, ENTITY> = DAOService<DTO, ENTITY>(this, dtoClass.getEntityModel())

    private var propertyBinderSource: PropertyBinder<DATA, ENTITY>? = null
    override val propertyBinder : PropertyBinder<DATA, ENTITY>
        get() = propertyBinderSource?:dtoClass.config.propertyBinder.castOrThrow<PropertyBinder<DATA, ENTITY>, InitException>()

    var hasContainer: DataModelContainer<DTO, DATA>? = null
    override val dataContainer: DataModelContainer<DTO, DATA>
        get() {
           return hasContainer ?:run {
               return DataModelContainer( dataModel,
                   dtoClass.config.dtoFactory.dataBlueprint.castOrThrow<ClassBlueprint<DATA>, InitException>(),
                   dtoClass.config.propertyBinder.castOrThrow<PropertyBinder<DATA, ENTITY>, InitException>())
           }
        }

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

    private var _regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>? = null
    private val regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>
        get(){
            return _regItem?:throw InitException("DtoClassRegistryItem uninitialized", ExceptionCode.UNDEFINED)
        }
    internal val registryItem: CommonDTORegistryItem<DTO, DATA, ENTITY> by lazy { regItem }
    internal var repositories =  MapBuilder<BindingKeyBase,  RepositoryBase<DTO, DATA, ENTITY, ModelDTO>> ()

    internal fun getRepository(key: BindingKeyBase):RepositoryBase<DTO, DATA, ENTITY, ModelDTO>{
       return repositories.map[key].getOrOperationsEx("Child repository not found @ $personalName", ExceptionCode.VALUE_NOT_FOUND)
    }

    fun getDtoRepositories():List<RepositoryBase<DTO, DATA, ENTITY, ModelDTO>>{
        return repositories.map.values.toList()
    }

    fun updateBinding(entity : ENTITY, updateMode: UpdateMode): CommonDTO<DTO ,DATA, ENTITY>{
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        if(updateMode == UpdateMode.ENTITY_TO_MODEL){
            dataContainer.setDataModelId(entity.id.value)
            daoService.setActiveEntity(entity)
        }
        initStatus = DTOInitStatus.INITIALIZED
        return this
    }

   internal fun initialize(
       regItem: DTORegistryItem<DTO, DATA, ENTITY>,
       container : DataModelContainer<DTO, DATA>,
       binder: PropertyBinder<DATA,ENTITY>)
   {
       _regItem =  CommonDTORegistryItem(dtoClass,  regItem.dataKClass, regItem.entityKClass, regItem.commonDTOKClass, this)
       propertyBinderSource = binder
       hasContainer = container
       dataContainer.attachBinder(propertyBinder)
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       personalName = "${regItem.commonDTOKClass.simpleName.toString()}[CommonDTO]"
       selfRegistration(registryItem)
    }

    companion object{
        val dtoRegistry: MapBuilder<String, CommonDTORegistryItem<*,*,*>> = MapBuilder<String, CommonDTORegistryItem<*,*,*>>()
        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: ExposifyEntityBase> selfRegistration(
            regItem :  CommonDTORegistryItem<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }
    }
}