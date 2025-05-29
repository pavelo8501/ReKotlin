package po.exposify.dto.components

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.extensions.subTask
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.CallbackRegistry
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KClass
import kotlin.reflect.KType




class DTOFactory<DTO, DATA, ENTITY>(
    private val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private val typeRegistry : TypeRegistry
): IdentifiableComponent,  TasksManaged where DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity {

    enum class FactorySubscriptions(override val value: Int) : ValueBased{
        ON_CREATED(1),
        ON_INITIALIZED(2)
    }

    val  dataModelClass : KClass<DATA> get() = typeRegistry.getRecord<DATA, OperationsException>(ComponentType.DATA_MODEL).clazz
    val  dtoKClass: KClass<DTO> get() = typeRegistry.getRecord<DTO, OperationsException>(ComponentType.DTO).clazz

    val config: DTOConfig<DTO, DATA, ENTITY>
        get() = dtoClass.config
    override val qualifiedName: String by lazy {
        "DTOFactory[${typeRegistry.getRecord<DATA, OperationsException>(ComponentType.DTO)?.simpleName}]"
    }
    override val type: ComponentType = ComponentType.Factory


    val notificator = CallbackRegistry()

    internal val dataBlueprint: ClassBlueprint<DATA> = ClassBlueprint(dataModelClass)
    val dtoBlueprint: ClassBlueprint<DTO> = ClassBlueprint(dtoKClass)

    @LogOnFault
    var dataModelBuilderFn: (() -> DATA)? = null


    @LogOnFault
    val typedSerializers: MutableMap<String, SerializerInfo<*>> = mutableMapOf()

    fun setDataModelConstructor(dataModelBuilder: (() -> DATA)) {
        dataModelBuilderFn = dataModelBuilder
    }

    fun serializerLookup(propertyName: String, type: KType):SerializerInfo<*>?{
        return if(typedSerializers.containsKey(propertyName)){
            typedSerializers[propertyName]
        }else{
            dtoClass.serializerLookup(type)?.let {
                typedSerializers[propertyName] = it
                it
            }
        }
    }

    fun dtoPostCreation(dto: CommonDTO<DTO, DATA, ENTITY>): CommonDTO<DTO, DATA, ENTITY> =
        subTask("dtoPostCreation") { handler ->
            val result = if (config.trackerConfigModified) {
                dto.initialize(DTOTracker(dto, config.trackerConfig))
                dto.addTrackerInfo(CrudOperation.Initialize, this)
            } else {
                dto.addTrackerInfo(CrudOperation.Initialize, this)
                dto.initialize()
                notificator.trigger(FactorySubscriptions.ON_INITIALIZED)
                dto
            }
            result
        }.resultOrException()

    /**
     * Create new instance of DatModel injectable to the specific DTOFunctions<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    fun createDataModel(): DATA = subTask("Create DataModel") { handler ->
        val constructFn = dataModelBuilderFn
        val dataModel = if (constructFn != null) {
            constructFn.invoke()
        } else {
            dataBlueprint.setExternalParamLookupFn { param ->
                serializerLookup(param.name.toString(),  param.type)?.let {
                    Json.Default.decodeFromString(it.serializer, "[]")
                }?:run {
                   throw OperationsException("Requested parameter name: ${param.name} ${qualifiedName}", ExceptionCode.FACTORY_CREATE_FAILURE)
                }
//                val serializerInfo =  serializerLookup(param.name.toString(),  param.type)
//                    .getOrOperationsEx(
//                    """Requested parameter name: ${param.name}.
//                    ${qualifiedName}
//                    """.trimMargin()
//                )
               // Json.Default.decodeFromString(serializerInfo.serializer, "[]")
            }
            val result = dataBlueprint.getConstructor().callBy(dataBlueprint.getConstructorArgs())
            result
        }
        dataModel
    }.resultOrException()

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    fun createDto(withDataModel: DATA? = null): CommonDTO<DTO, DATA, ENTITY> =
        subTask("Create DTO") { handler ->
            dtoBlueprint.setExternalParamLookupFn { param ->
                when (param.name) {
                    "dataModel" -> {
                        if(withDataModel != null){
                            withDataModel
                        }else{
                            val result = createDataModel()
                            result
                        }
                    }
                    else -> {
                        throw OperationsException(
                            "Parameter ${param.name} unavailable when creating dataModel",
                            ExceptionCode.VALUE_NOT_FOUND
                        )
                    }
                }
            }
            val newDto = dtoBlueprint.getConstructor().callBy(dtoBlueprint.getConstructorArgs())
                .castOrOperationsEx<CommonDTO<DTO, DATA, ENTITY>>("Unable to cast DTO to CommonDTO<DTO, DATA, ENTITY")
            notificator.trigger(FactorySubscriptions.ON_CREATED)
            dtoPostCreation(newDto)
        }.resultOrException()
}