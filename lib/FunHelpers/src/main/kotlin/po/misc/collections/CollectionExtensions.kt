package po.misc.collections



/**
 * Builds a list by invoking the given [builder] function for each index from `0` until this value.
 *
 * The [builder] receives the current index, allowing you to generate values that depend on
 * their position in the resulting list.
 *
 * Example:
 * ```
 * val items = 5.repeatBuild { index ->
 *     "Item #$index"
 * }
 * // Produces: ["Item #0", "Item #1", "Item #2", "Item #3", "Item #4"]
 * ```
 *
 * @param builder A function that takes the current index (starting at 0) and produces a value of type [T].
 * @return A list containing [this] number of elements created by invoking [builder] for each index.
 */
inline fun <T: Any> Int.repeatBuild(builder: (Int)-> T): List<T>{
    val result = mutableListOf<T>()
    val thisSize = this

    for (i in 0..<thisSize){
        val built = builder(i)
        result.add(built)
    }
    return result
}