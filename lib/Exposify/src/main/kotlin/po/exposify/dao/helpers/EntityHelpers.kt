package po.exposify.dao.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.data.ColumnMetadata
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import kotlin.reflect.full.companionObjectInstance

inline fun <reified E : LongEntity, reified EX : ManagedException> getExposifyEntityCompanion(): ExposifyEntityClass<E> {
    val companion = E::class.companionObjectInstance
        ?: throw SelfThrownException.build<EX>("Missing companion object for ${E::class.simpleName}", ExceptionCode.REFLECTION_ERROR)
    val base = companion.castBaseOrThrow<ExposifyEntityClass<*>, EX>()
    return base.castOrThrow<ExposifyEntityClass<E>, InitException>()
}
