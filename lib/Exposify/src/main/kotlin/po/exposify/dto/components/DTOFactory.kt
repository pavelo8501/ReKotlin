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
import po.misc.serialization.SerializerInfo
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KClass
import kotlin.reflect.KType




class DTOFactory<DTO, DATA, ENTITY>(
    private val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private val dtoKClass : KClass<DTO>,
    private val dataModelClass : KClass<DATA>,
): IdentifiableComponent,  TasksManaged where DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity {

    private val config: DTOConfig<DTO, DATA, ENTITY>
        get() = dtoClass.config
    override val qualifiedName: String by lazy {
        "DTOFactory[${config.registryRecord.dtoName}]"
    }
    override val type: ComponentType = ComponentType.Factory

    internal val dataBlueprint: ClassBlueprint<DATA> = ClassBlueprint(dataModelClass)
    private val dtoBlueprint: ClassBlueprint<DTO> = ClassBlueprint(dtoKClass)

    @LogOnFault
    var dataModelBuilderFn: (() -> DATA)? = null


    @LogOnFault
    private val typedSerializers: MutableMap<String, SerializerInfo<*>> = mutableMapOf()

    fun setDataModelConstructor(dataModelBuilder: (() -> DATA)) {
        dataModelBuilderFn = dataModelBuilder
    }

    private fun serializerLookup(propertyName: String, type: KType):SerializerInfo<*>?{
        return if(typedSerializers.containsKey(propertyName)){
            typedSerializers[propertyName]
        }else{
            dtoClass.serializerLookup(type)?.let {
                typedSerializers[propertyName] = it
                it
            }
        }
    }

    private fun dtoPostCreation(dto: CommonDTO<DTO, DATA, ENTITY>): CommonDTO<DTO, DATA, ENTITY> =
        subTask("dtoPostCreation") { handler ->
            val result = if (config.trackerConfigModified) {
                dto.initialize(DTOTracker(dto, config.trackerConfig))
                dto.addTrackerInfo(CrudOperation.Initialize, this)
            } else {
                dto.addTrackerInfo(CrudOperation.Initialize, this)
                dto.initialize()
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
                val serializerInfo =  serializerLookup(param.name.toString(),  param.type)
                    .getOrOperationsEx(
                    """Requested parameter name: ${param.name}.
                    ${qualifiedName}    
                    """.trimMargin()
                )
                Json.Default.decodeFromString(serializerInfo.serializer, "[]")
            }
            dataBlueprint.getConstructor().callBy(dataBlueprint.getConstructorArgs())
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
            val dataModel = withDataModel ?: createDataModel()
            dtoBlueprint.setExternalParamLookupFn { param ->
                when (param.name) {
                    "dataModel" -> {
                        dataModel
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
            dtoPostCreation(newDto)
        }.resultOrException()
}