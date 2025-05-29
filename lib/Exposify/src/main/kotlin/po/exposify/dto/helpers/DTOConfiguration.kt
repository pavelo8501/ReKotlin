package po.exposify.dto.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.reflection.properties.PropertyMap
import po.misc.registries.type.TypeRegistry
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import kotlin.reflect.full.companionObjectInstance

inline fun <reified E : LongEntity, reified EX : ManagedException> getExposifyEntityCompanion(): ExposifyEntityClass<E> {
    val companion = E::class.companionObjectInstance
        ?: throw SelfThrownException.build<EX>("Missing companion object for ${E::class.simpleName}", 0)
    val base = companion.castBaseOrThrow<ExposifyEntityClass<*>, EX>()
    return base.castOrThrow<ExposifyEntityClass<E>, InitException>()
}


inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.configuration(
    noinline block:  DTOConfig<DTO, D, E>.() -> Unit
): Unit where DTO: ModelDTO, D: DataModel, E: LongEntity {

    val registry = TypeRegistry()
    val dtoTypeRec = registry.addRecord<DTO>(ComponentType.DTO)
    val dataTypeRec = registry.addRecord<D>(ComponentType.DATA_MODEL)
    val entityTypeRec = registry.addRecord<E>(ComponentType.ENTITY)

    val propertyMap = PropertyMap()
    propertyMap.applyClass(ComponentType.DATA_MODEL, dataTypeRec.clazz, dataTypeRec)
    propertyMap.applyClass(ComponentType.ENTITY, entityTypeRec.clazz, entityTypeRec)

    val entityModel =  getExposifyEntityCompanion<E, InitException>()
    val newConfiguration = DTOConfig(registry, propertyMap, entityModel, this)
    configParameter = newConfiguration
    block.invoke(config)
    setupValidation(propertyMap, registry)
    initialized = true
}
