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
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ModuleType
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.extensions.subTask
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.types.TypeRecord
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import kotlin.reflect.KClass
import kotlin.reflect.KType

class DTOFactory<DTO, DATA, ENTITY>(
    private val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private val typeRegistry : TypeRegistry,
    val moduleType : ModuleType.DTOFactory = ModuleType.DTOFactory
): IdentifiableModule by moduleType,  TasksManaged where DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity {

    enum class FactoryEvents(override val value: Int) : ValueBased{
        ON_CREATED(1),
        ON_INITIALIZED(2)
    }
    val notificator = TypedCallbackRegistry<CommonDTO<DTO, DATA, ENTITY>, Unit>()

    val  dataType : TypeRecord<DATA> get() = typeRegistry.getRecord<DATA, OperationsException>(SourceObject.Data)
    val  dtoType: TypeRecord<DTO> get() = typeRegistry.getRecord<DTO, OperationsException>(SourceObject.DTO)

    val config: DTOConfig<DTO, DATA, ENTITY>
        get() = dtoClass.config

    internal val dataBlueprint: ClassBlueprint<DATA> = ClassBlueprint(dataType.clazz)
    val dtoBlueprint: ClassBlueprint<DTO> = ClassBlueprint(dtoType.clazz)

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
                notificator.triggerForAll(FactoryEvents.ON_INITIALIZED, dto)
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
                   throw OperationsException("Requested parameter name: ${param.name} ${completeName}", ExceptionCode.FACTORY_CREATE_FAILURE)
                }
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
            notificator.triggerForAll(FactoryEvents.ON_CREATED, newDto)
            dtoPostCreation(newDto)
        }.resultOrException()
}