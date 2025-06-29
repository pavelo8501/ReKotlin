package po.exposify.dao.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManageable
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import kotlin.reflect.full.companionObjectInstance

inline fun <reified E : LongEntity, reified EX : ManagedException> getExposifyEntityCompanion(): ExposifyEntityClass<E> {
    val companion = E::class.companionObjectInstance
    if (companion != null) {
        val casted = companion.castBaseOrThrow<ExposifyEntityClass<E>, EX>(null) {
            ManageableException.build<EX, ExceptionCode>(it, ExceptionCode.REFLECTION_ERROR)
        }
        return casted
    } else {
        throwManageable<EX, ExceptionCode>(
            "Missing companion object for ${E::class.simpleName}",
            ExceptionCode.REFLECTION_ERROR
        )
    }
}
