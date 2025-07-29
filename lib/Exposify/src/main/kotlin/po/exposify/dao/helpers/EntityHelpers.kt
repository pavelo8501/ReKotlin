package po.exposify.dao.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.exceptions.operationsException
import po.misc.context.CTX
import po.misc.types.castBaseOrThrow
import kotlin.reflect.full.companionObjectInstance

inline fun <reified E : LongEntity> getExposifyEntityCompanion(
    callingContext: Any
): ExposifyEntityClass<E> {
    val companion = E::class.companionObjectInstance
    if (companion != null) {
        val a = "stop"
        return companion.castBaseOrThrow<ExposifyEntityClass<E>>(callingContext) { payload ->
            initException(payload.setCode(ExceptionCode.REFLECTION_ERROR))
        }
    } else {
        throw operationsException(
            "Missing companion object for ${E::class.simpleName}",
            ExceptionCode.REFLECTION_ERROR,
            callingContext
        )
    }
}
