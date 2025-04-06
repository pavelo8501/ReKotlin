package po.exposify.wrappers


import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import kotlin.reflect.KMutableProperty1


class NullablePropertyWrapper<T : Any, V : Any?> {

    private var property: KMutableProperty1<T, V>? = null
    private var isNullable: Boolean = false

    fun inject(property: KMutableProperty1<T, V>) {
        this.property = property
        this.isNullable = property.returnType.isMarkedNullable
    }

    fun extract(): KMutableProperty1<T, V> {
        return property ?: throw OperationsException(
            "Extracting property undefined",
            ExceptionCode.NOT_INITIALIZED
        )
    }

    fun get(receiver: T): V {
        return property?.get(receiver) ?: throw OperationsException(
            "Trying to get from uninitialized property",
            ExceptionCode.NOT_INITIALIZED
        )
    }

    fun set(receiver: T, value: V) {
        if (!isNullable && value == null) {
            throw OperationsException("Cannot set null on non-nullable property", ExceptionCode.NOT_INITIALIZED)
        }
        property?.set(receiver, value)
    }

    val nullable: Boolean
        get() = isNullable
}
