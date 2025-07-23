package po.exposify.dto.components.bindings.relation_binder.components

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
        return property ?: throw  Exception("Extracting property undefined")
    }

    fun get(receiver: T): V {
        return property?.get(receiver) ?: throw  Exception("Extracting property undefined")
    }

    fun set(receiver: T, value: V) {
        if (!isNullable && value == null) {
            throw  Exception("Extracting property undefined")
        }
        property?.set(receiver, value)
    }

    val nullable: Boolean
        get() = isNullable
}