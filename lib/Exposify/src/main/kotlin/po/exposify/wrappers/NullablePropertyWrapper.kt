package po.exposify.wrappers

import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import kotlin.reflect.KMutableProperty1


class NullablePropertyWrapper<DATA, CHILD_DATA>{

    private var nonNullable: KMutableProperty1<DATA, CHILD_DATA>? = null
    private var nullable: KMutableProperty1<DATA, CHILD_DATA?>? = null

    fun inject(property: KMutableProperty1<DATA, CHILD_DATA>) {
        nonNullable = property
    }

    fun extract():KMutableProperty1<DATA, CHILD_DATA> {
        return nonNullable?:throw OperationsException(
            "Extracting NonNullable property undefined",
            ExceptionCodes.NOT_INITIALIZED)
    }

    fun injectNullable(property: KMutableProperty1<DATA, CHILD_DATA?>){
        nullable = property
    }

    fun extractNullable():KMutableProperty1<DATA, CHILD_DATA?> {
        return nullable?:throw OperationsException(
            "Extracting Nullable property undefined",
            ExceptionCodes.NOT_INITIALIZED)
    }

    fun get(receiver: DATA): CHILD_DATA? =
        nonNullable?.get(receiver) ?: nullable?.get(receiver)

    fun set(receiver: DATA, value: CHILD_DATA?) {
        value?.let {
            nonNullable?.set(receiver, it) ?: nullable?.set(receiver, it)
        }
    }
}