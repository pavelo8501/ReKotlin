package po.exposify.dto.components

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.extensions.runAction
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.callbackManager
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.context.asSubIdentity
import po.misc.functions.registries.TaggedLambdaRegistry
import po.misc.functions.registries.lambdaRegistryOf
import po.misc.interfaces.ValueBased
import po.misc.serialization.SerializerInfo
import po.misc.types.TypeData
import kotlin.reflect.KType

sealed class DTOFactoryBase<DTO, D, E>(
    protected val dtoConfiguration: DTOConfig<DTO, D, E>,
) : TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {
    enum class Events(
        override val value: Int,
    ) : ValueBased {
        OnCreated(1),
        OnInitialized(2),
    }

    protected val eventTypeKey: Class<Events> = Events::class.java
    protected val commonDTOType: CommonDTOType<DTO, D, E> get() = dtoConfiguration.commonDTOType
    abstract override val identity: CTXIdentity<out CTX>

    val dtoType: TypeData<DTO> get() = commonDTOType.dtoType
    val dataType: TypeData<D> get() = commonDTOType.dataType

    val notifier =
        callbackManager<Events>(
            { CallbackManager.createPayload<Events, CommonDTO<DTO, D, E>>(this, Events.OnInitialized) },
        )

    val onCreatedPayload =
        CallbackManager.createPayload<Events, CommonDTO<DTO, D, E>>(notifier, Events.OnCreated)

    internal val dataBlueprint: ClassBlueprint<D> = ClassBlueprint(commonDTOType.dataType.kClass)
    val dtoBlueprint: ClassBlueprint<DTO> = ClassBlueprint(commonDTOType.dtoType.kClass)

    var dataModelBuilderFn: (() -> D)? = null

    @LogOnFault
    val typedSerializers: MutableMap<String, SerializerInfo<*>> = mutableMapOf()

    fun setDataModelConstructor(dataModelBuilder: (() -> D)) {
        dataModelBuilderFn = dataModelBuilder
    }

    fun serializerLookup(
        propertyName: String,
        type: KType,
    ): SerializerInfo<*>? {
        return typedSerializers.getOrPut(propertyName) {
            dtoConfiguration.serializerLookup(type)
                ?: return null
        }

//        return if (typedSerializers.containsKey(propertyName)) {
//            typedSerializers[propertyName]
//        } else {
//            dtoConfiguration.serializerLookup(type)?.let {
//                typedSerializers[propertyName] = it
//                it
//            }
//        }
    }

    /**
     * Create new instance of DatModel injectable to the specific DTOFunctions<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    fun createDataModel(): D =
        runAction("Create DataModel", dataType.kType) {
            val constructFn = dataModelBuilderFn
            val dataModel =
                if (constructFn != null) {
                    constructFn.invoke()
                } else {
                    dataBlueprint.setExternalParamLookupFn { param ->
                        serializerLookup(param.name.toString(), param.type)?.let {
                            Json.Default.decodeFromString(it.serializer, "[]")
                        } ?: run {
                            val errorMsg = "Constructor param ${param.name} not found"
                            throw operationsException(errorMsg, ExceptionCode.REFLECTION_ERROR, this)
                        }
                    }
                    val result = dataBlueprint.getConstructor().callBy(dataBlueprint.getConstructorArgs())
                    result
                }
            dataModel
        }

    abstract fun createDto(withDataModel: D? = null): CommonDTO<DTO, D, E>
}

class DTOFactory<DTO, D, E>(
    dtoConfiguration: DTOConfig<DTO, D, E>,
) : DTOFactoryBase<DTO, D, E>(dtoConfiguration) where DTO : ModelDTO, D : DataModel, E : LongEntity {
    override val identity: CTXIdentity<DTOFactory<DTO, D, E>> = asIdentity()

    private fun dtoPostCreation(dto: CommonDTO<DTO, D, E>): CommonDTO<DTO, D, E> =
        runAction("dtoPostCreation", commonDTOType.dtoType.kType) {
            val result =
                if (dtoConfiguration.trackerConfigModified) {
                    dto.initialize(dtoConfiguration.trackerConfig)
                } else {
                    dto.initialize()
                    notifier.trigger(Events.OnInitialized, dto)
                    dto
                }
            result
        }

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    override fun createDto(withDataModel: D?): CommonDTO<DTO, D, E> =
        runAction("Create DTO", dtoType.kType) {
            dtoBlueprint.setExternalParamLookupFn { param ->
                when (param.name) {
                    "dataModel" -> {
                        if (withDataModel != null) {
                            withDataModel
                        } else {
                            val result = createDataModel()
                            result
                        }
                    }

                    else -> {
                        val errorMessage = "Parameter ${param.name} unavailable when creating dataModel"
                        throw OperationsException(errorMessage, ExceptionCode.REFLECTION_ERROR, this, null)
                    }
                }
            }
            val newDto = dtoBlueprint.getConstructor().callBy(dtoBlueprint.getConstructorArgs())
            val asCommonDTO = newDto.castOrOperations<CommonDTO<DTO, D, E>>(this)

            notifier.trigger(Events.OnCreated, asCommonDTO)
            dtoPostCreation(asCommonDTO)
        }
}

class CommonDTOFactory<DTO, D, E, F, FD, FE>(
    hostingDTO: CommonDTO<F, FD, FE>,
    dtoConfiguration: DTOConfig<DTO, D, E>,
) : DTOFactoryBase<DTO, D, E>(dtoConfiguration)
    where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    override val identity: CTXIdentity<CommonDTOFactory<DTO, D, E, F, FD, FE>> = asSubIdentity(this, hostingDTO)

    val onDTOCreated: TaggedLambdaRegistry<Events, CommonDTO<DTO, D, E>> = lambdaRegistryOf(Events.OnCreated)

    private fun dtoPostCreation(dto: CommonDTO<DTO, D, E>): CommonDTO<DTO, D, E> =
        runAction("dtoPostCreation", commonDTOType.dtoType.kType) {
            val result =
                if (dtoConfiguration.trackerConfigModified) {
                    dto.initialize(dtoConfiguration.trackerConfig)
                } else {
                    dto.initialize()
                    notifier.trigger(Events.OnInitialized, dto)
                    dto
                }
            onDTOCreated.trigger(Events.OnCreated, dto)
            result
        }

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    override fun createDto(withDataModel: D?): CommonDTO<DTO, D, E> =
        runAction("Create DTO", dtoType.kType) {
            dtoBlueprint.setExternalParamLookupFn { param ->
                when (param.name) {
                    "dataModel" -> {
                        if (withDataModel != null) {
                            withDataModel
                        } else {
                            val result = createDataModel()
                            result
                        }
                    }
                    else -> {
                        val errorMessage = "Parameter ${param.name} unavailable when creating dataModel"
                        throw OperationsException(errorMessage, ExceptionCode.REFLECTION_ERROR, this, null)
                    }
                }
            }
            val newDto = dtoBlueprint.getConstructor().callBy(dtoBlueprint.getConstructorArgs())
            val asCommonDTO = newDto.castOrOperations<CommonDTO<DTO, D, E>>(this)
            if (withDataModel != null) {
                asCommonDTO.dataContainer.provideValue(withDataModel)
            }
            notifier.trigger(Events.OnCreated, asCommonDTO)
            dtoPostCreation(asCommonDTO)
        }
}
