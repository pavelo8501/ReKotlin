package po.exposify.wrappers

import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
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
            ExceptionCodes.NOT_INITIALIZED
        )
    }

    fun get(receiver: T): V {
        return property?.get(receiver) ?: throw OperationsException(
            "Trying to get from uninitialized property",
            ExceptionCodes.NOT_INITIALIZED
        )
    }

    fun set(receiver: T, value: V) {
        if (!isNullable && value == null) {
            throw OperationsException("Cannot set null on non-nullable property", ExceptionCodes.NOT_INITIALIZED)
        }
        property?.set(receiver, value)
    }

    val nullable: Boolean
        get() = isNullable
}


//class NullablePropertyWrapper<T : Any, V: Any?> {
//
//    private var property: KMutableProperty1<T, V & Any>? = null
//    private var isNullable: Boolean = false
//
//    private var nonNullable: KMutableProperty1<T, V & Any>? = null
//    private var nullable: KMutableProperty1<T, V?>? = null
//
//    fun inject(property: KMutableProperty1<T, V & Any>) {
//        this.nonNullable = property
//        this.isNullable = false
//    }
//
//    fun injectNullable(property: KMutableProperty1<T, V?>) {
//        this.nullable = property
//        this.isNullable = true
//    }
//
//
//    fun get(receiver: T): V {
//        return property?.get(receiver) ?: throw OperationsException(
//            "Trying to get from uninitialized property",
//            ExceptionCodes.NOT_INITIALIZED
//        )
//    }
//
//    fun set(receiver: T, value: V) {
//        value?.let {
//            nonNullable?.set(receiver, it) ?: nullable?.set(receiver, it)
//        }
//    }
//}



//class NullablePropertyWrapper<T>() where  T: Any {
//    private var nonNullable: KMutableProperty1<T, Any>? = null
//    private var nullable: KMutableProperty1<T, Any?>? = null
//
//    fun inject(property: KMutableProperty1<T, Any>) {
//        nonNullable = property
//    }
//
//    fun extract():KMutableProperty1<T, Any> {
//        return nonNullable?:throw OperationsException(
//            "Extracting NonNullable property undefined",
//            ExceptionCodes.NOT_INITIALIZED)
//    }
//
//    fun injectNullable(property: KMutableProperty1<T, Any?>){
//        nullable = property
//    }
//
//    fun extractNullable():KMutableProperty1<T, Any?> {
//        return nullable?:throw OperationsException(
//            "Extracting Nullable property undefined",
//            ExceptionCodes.NOT_INITIALIZED)
//    }
//
//    fun get(receiver: T): Any? =
//        nonNullable?.get(receiver) ?: nullable?.get(receiver)
//
//    fun set(receiver: T, value: Any?) {
//        value?.let {
//            nonNullable?.set(receiver, it) ?: nullable?.set(receiver, it)
//        }
//    }
//}