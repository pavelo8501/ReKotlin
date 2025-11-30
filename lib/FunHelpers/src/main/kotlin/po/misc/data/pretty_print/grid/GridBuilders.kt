package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


/**
 * Creates a reusable grid template for rendering a collection property of a receiver type [T].
 *
 * This function is part of the “Pretty Grid” templating system and is intended to simplify
 * reusable grid definitions for any `T` that owns a collection of items `Collection<V>`.
 *
 * Usage:
 *  - You pass a property reference (`KProperty1<T, Collection<V>>`) that extracts the list
 *    of elements from the receiver.
 *  - You define a `onReceiverAvailable` lambda which receives a [PrettyGridBase] instance
 *    and the extracted list of items. This lambda sets up the rows and cells that make up
 *    the grid template.
 *  - The returned [PrettyPromiseGrid] can later be applied to any receiver via
 *    [PrettyPromiseGrid.processReceiver], which will:
 *      1) resolve the collection from the receiver
 *      2) execute your template builder
 *      3) build rendered rows
 *
 * This allows the template to be defined once and reused for multiple receivers, while
 * preserving the correct element type through [TypeToken].
 *
 * @param T The receiver type owning the collection.
 * @param V The element type contained in the collection.
 * @param property Reference to the collection property on [T].
 * @param rowOptions Optional default row configuration.
 * @param onReceiverAvailable Builder block invoked when the receiver’s list is loaded.
 *
 * @return A [PrettyPromiseGrid] that encapsulates the reusable grid template.
 */
inline fun <reified T: Any, reified V: Any> buildPrettyGrid(
    property: KProperty1<T,  Collection<V>>,
    rowOptions: RowOptions? = null,
    noinline onReceiverAvailable: PrettyPromiseGrid<T, V>.(List<V>)-> Unit,
): PrettyPromiseGrid<T, V> {
    val options = rowOptions?: RowOptions()
    val token = TypeToken.create<T>()
    val valueToken = TypeToken.create<V>()
    val grid = PrettyPromiseGrid(token,  valueToken, property, options, onReceiverAvailable)
    return grid
}


/**
 * Creates and configures a new [PrettyGrid] instance for the reified type [T]
 * using the provided [builder] DSL block.
 *
 * This function initializes a type-safe grid by generating a [TypeToken] for [T]
 * and then applying the configuration defined inside the [builder] lambda.
 *
 * ### Example
 * ```
 * val grid = buildPrettyGrid<MyRowType> {
 *     column("Name") { it.name }
 *     column("Age")  { it.age }
 * }
 * ```
 *
 * @param T The data type the grid operates on.
 * @param builder A DSL block used to configure the created [PrettyGrid].
 * @return The configured [PrettyGrid] instance.
 */
inline fun <reified T: Any> buildPrettyGrid(
    rowOptions: RowOptions? = null,
    builder: PrettyGrid<T>.() -> Unit
):PrettyGrid<T>{
    val options = rowOptions?: RowOptions()
    val token = TypeToken.create<T>()
    val grid = PrettyGrid(token, options)
    builder.invoke(grid)
    return grid
}