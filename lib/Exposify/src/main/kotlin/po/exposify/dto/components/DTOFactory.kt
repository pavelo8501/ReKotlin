package po.exposify.dto.components

import kotlinx.serialization.KSerializer
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
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.result.onFailureCause
import po.lognotify.extensions.subTask
import po.misc.types.getKType
import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType


inline fun <reified S: KSerializer<T>, T>  S.serializerInfo(
    propertyName: String,
    isListSerializer: Boolean = true
):SerializerInfo{
    return SerializerInfo(propertyName, this, this.getKType(), isListSerializer)
}

data class SerializerInfo(
    val dataClassPropertyName: String,
    val serializer : KSerializer<*>,
    val type: KType,
    val isListSerializer: Boolean = true
){

}


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

    internal val dataBlueprint : ClassBlueprint<DATA> =  ClassBlueprint(dataModelClass)
    private val dtoBlueprint : ClassBlueprint<DTO> = ClassBlueprint(dtoKClass)

    @LogOnFault
    var dataModelBuilderFn : (() -> DATA)? = null

    @LogOnFault
    var serializers = mutableMapOf<String, KSerializer<*>>()

    internal val typedSerializers : MutableMap<String, SerializerInfo> = mutableMapOf()

    fun hasListSerializer(name: String): SerializerInfo?{
       val info  = typedSerializers.keys.firstOrNull { it == name }
       return typedSerializers[info]
    }
    fun <T> provideListSerializer(propertyName : String, serializerInfo : SerializerInfo){
        typedSerializers[propertyName] = serializerInfo
    }

    fun <T> setSerializableType(name: String, serializer: KSerializer<T>){
        serializers[name] = serializer
    }

    fun setDataModelConstructor(dataModelBuilder: (() -> DATA)){
        dataModelBuilderFn = dataModelBuilder
    }


    private suspend fun dtoPostCreation(dto: CommonDTO<DTO, DATA, ENTITY>)
        = subTask("dtoPostCreation"){handler->

        if(config.trackerConfigModified){
            dto.initialize(DTOTracker(dto, config.trackerConfig))
            dto.addTrackerInfo(CrudOperation.Initialize, this)
        }else{
            dto.addTrackerInfo(CrudOperation.Initialize, this)
            dto.initialize()
        }
    }

    @LogOnFault
    var activeKParameterLookup :  KParameter? = null

    /**
     * Create new instance of DatModel injectable to the specific DTOFunctions<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    suspend fun createDataModel():DATA
      = subTask("Create DataModel") {
        val constructFn = dataModelBuilderFn
        val dataModel = if (constructFn != null) {
            constructFn.invoke()
        } else {
            dataBlueprint.setExternalParamLookupFn { param ->
                activeKParameterLookup = param
                var paramValue: Any? = null
                val keysRegistered = typedSerializers.keys.joinToString(", ", "[", "]") { it }
                val serializerInfo = typedSerializers[param.name.toString()].getOrOperationsEx(
                    "Requested parameter name: ${param.name}. Registered keys : $keysRegistered"
                )

                runCatching {
                    paramValue = Json.Default.decodeFromString(serializerInfo.serializer, "[]")
                }.onFailure {
                    throw it
                }
                paramValue
            }
            val args = dataBlueprint.getConstructorArgs()
            val constructor = dataBlueprint.getConstructor()
            constructor.callBy(args)
        }
        dataModel
    }.onFailureCause {
        val throwable = it
    }.resultOrException()

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    suspend fun createDto(withDataModel : DATA? = null): CommonDTO<DTO, DATA, ENTITY> =
        subTask("Create DTO"){handler->
        val dataModel = withDataModel?: createDataModel()
        dtoBlueprint.setExternalParamLookupFn { param ->
            when (param.name) {
                "dataModel" -> {
                    if (param.type.classifier == dataModel::class) {
                        dataModel
                    } else {
                        throw IllegalArgumentException("Mismatched dataModel type for ${dtoBlueprint.clazz.simpleName}. Expected ${param.type}, got ${dataModel::class}")
                    }
                }
                else -> {
                    null
                }
            }
        }
       try {
            val args = dtoBlueprint.getConstructorArgs()
            val newDto =  dtoBlueprint.getConstructor().callBy(args).castOrOperationsEx<CommonDTO<DTO, DATA, ENTITY>>()
            dtoPostCreation(newDto)
            newDto
        }catch (th: Throwable){
           val argsUsed = dtoBlueprint.getConstructorArgs()
           val argsStr =  argsUsed.keys.joinToString { it.name.toString() }
           handler.warn("While creating dto for $argsStr")
           handler.warn("Arguments used ${argsUsed.values.map { toString() }}")
          handler.warn(th.toString().prependIndent("Exception"))
          throw  th
        }
    }.onFailureCause {

        }.resultOrException()

}