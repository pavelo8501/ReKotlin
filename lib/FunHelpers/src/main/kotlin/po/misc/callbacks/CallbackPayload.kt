package po.misc.callbacks

import po.misc.interfaces.ValueBased

//
///**
// * Sealed base class for all callback payload types.
// *
// * @param T The input type accepted by the callback function.
// * @param R The return type produced by the callback function.
// * @property event The associated event identifier.
// * @property callback The actual callback function to be invoked.
// */
//sealed class CallbackPayloadBase<T, R: Any>(
//    val event: ValueBased
//){
//    /**
//     * The callback function to be triggered when this payload is executed.
//     */
//    abstract val callback: (T)-> R
//
//    abstract fun trigger(value:T):R
//}
//
///**
// * A basic callback payload for single-argument callbacks that return Unit.
// *
// * @param T The input type for the callback function.
// * @property callback A function taking one argument and returning Unit.
// * @property event The associated event identifier.
// */
//class CallbackPayload<T: Any>(
//    override val callback: (T)-> Unit,
//    event: ValueBased
//): CallbackPayloadBase<T, Unit>(event){
//
//    override fun trigger(value: T){
//        callback.invoke(value)
//    }
//
//    companion object {
//        /**
//         * Creates a new [CallbackPayload] instance.
//         *
//         * @param event The event identifier to associate with this callback.
//         * @param callback The callback function accepting a single argument.
//         * @return A new instance of [CallbackPayload].
//         */
//        fun <T: Any> create(
//        event: ValueBased,
//        callback: (T) -> Unit
//        ): CallbackPayload<T> = CallbackPayload(callback, event)
//    }
//}
//
//
//enum class Test(override val value: Int): ValueBased{
//    A(1)
//}
//
//
//
///**
// * A payload for two-argument callbacks that return Unit.
// *
// * @param T1 The first argument type.
// * @param T2 The second argument type.
// * @property callback A function accepting a pair of arguments and returning Unit.
// * @property event The associated event identifier.
// */
//class DoubleCallbackPayload<T1, T2>(
//    override val callback: (Pair<T1, T2>)-> Unit,
//    event: ValueBased
//): CallbackPayloadBase<Pair<T1, T2>, Unit>(event) where T1: Any, T2: Any{
//
//
//
//    override fun trigger(value: Pair<T1, T2>) {
//        callback.invoke(value)
//    }
//
//    companion object {
//
//        /**
//         * Creates a new [DoubleCallbackPayload] instance from a two-argument function.
//         *
//         * @param event The event identifier to associate with this callback.
//         * @param callback A function accepting two arguments.
//         * @return A new instance of [DoubleCallbackPayload].
//         */
//        fun <T1: Any, T2: Any> create(
//            event: ValueBased,
//            callback: (T1, T2) -> Unit
//        ): DoubleCallbackPayload<T1, T2> =
//            DoubleCallbackPayload({ pair -> callback(pair.first, pair.second) }, event)
//    }
//
//}
//
//
///**
// * A callback payload for single-argument functions that return a result.
// *
// * @param T The input type.
// * @param R The result type.
// * @property callback A function accepting one argument and returning a value of type [R].
// * @property event The associated event identifier.
// */
//class ResultCallbackPayload<T: Any, R: Any>(
//    override val callback: (T)-> R,
//    event: ValueBased
//): CallbackPayloadBase<T, R>(event){
//
//
//    override fun trigger(value: T):R {
//       return callback.invoke(value)
//    }
//
//    /**
//     * Creates a new [ResultCallbackPayload] instance.
//     *
//     * @param event The event identifier.
//     * @param callback A function accepting a single argument and returning a result.
//     * @return A new instance of [ResultCallbackPayload].
//     */
//    companion object {
//        fun <T: Any, R: Any> create(
//            event: ValueBased,
//            callback: (T) -> R
//        ): ResultCallbackPayload<T, R> = ResultCallbackPayload(callback, event)
//    }
//}
//
//
///**
// * A callback payload for two-argument functions that return a result.
// *
// * @param T1 The first argument type.
// * @param T2 The second argument type.
// * @param R The result type.
// * @property callback A function accepting a pair of arguments and returning a value of type [R].
// * @property event The associated event identifier.
// */
//class ResultDoubleCallbackPayload<T1, T2, R>(
//    override val callback: (Pair<T1, T2>)-> R,
//    event: ValueBased
//): CallbackPayloadBase<Pair<T1, T2>, R>(event) where T1: Any, T2: Any, R:Any{
//
//
//    override fun trigger(value: Pair<T1, T2>):R {
//       return callback.invoke(value)
//    }
//
//    companion object {
//
//        /**
//         * Creates a new [ResultDoubleCallbackPayload] instance from a two-argument function.
//         *
//         * @param event The event identifier.
//         * @param callback A function accepting two arguments and returning a result.
//         * @return A new instance of [ResultDoubleCallbackPayload].
//         */
//        fun <T1: Any, T2: Any, R: Any> create(
//            event: ValueBased,
//            callback: (T1, T2) -> R
//        ): ResultDoubleCallbackPayload<T1, T2, R> =
//            ResultDoubleCallbackPayload({ pair -> callback(pair.first, pair.second) }, event)
//    }
//}

