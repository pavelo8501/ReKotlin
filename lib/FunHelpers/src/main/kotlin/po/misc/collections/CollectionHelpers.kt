package po.misc.collections

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


fun <T: Any> List<T>.exactlyOneOrThrow(exceptionProvider:()-> Throwable):T{
    if(size != 1){
       throw exceptionProvider()
    }else{
        return this[0]
    }
}

fun <T: Any> T?.asList(): List<T>{
   return if (this != null){
        listOf(this)
    }else{
        emptyList<T>()
    }
}

fun <T> Array<out T>.toList(element:T): List<T> {
    return buildList {
        add(element)
        addAll(this@toList)
    }
}

fun <T> Array<out T>.toListOrDefaultIfEmpty(provider: () -> T): List<T> {
    val thisList = toList()
    if(thisList.isEmpty()){
        return provider().asList()
    }
    return buildList {
        addAll(thisList)
    }
}


fun <T> MutableList<T>.addNotNull(element:T?){
    if(element != null){
        this.add(element)
    }
}

fun <T> MutableList<T>.addAllNotNull(elements: List<T>?){
    if(elements != null){
        this.addAll(elements)
    }
}



fun MutableList<String>.addNotBlank(string: String?, ifBlank: (() -> Unit)? = null): Boolean{
  return if(!string.isNullOrBlank()){
        add(string)
    }else{
        ifBlank?.invoke()
        false
    }
}

/**
 * Attempts to insert the given [value] under [key] if the key is not already present.
 *
 * If the key is absent:
 *  - The [value] is inserted and returned.
 *
 * If the key already exists:
 *  - The existing value is passed to [failureAction], and the result of that callback
 *    is returned instead.
 *
 * This helper enables "conditional overwrite" patterns, where the caller decides
 * whether an existing mapping should be kept, replaced, or ignored.
 *
 * @param key The key to check.
 * @param value The value to insert if no existing mapping is found.
 * @param failureAction Called when the key already exists; receives the existing value
 *                      and may return an alternative value or `null`.
 * @return The inserted value, or the result of [failureAction] for existing entries.
 */
fun <K: Any, V: Any>  MutableMap<K, V>.putIfAbsentOr(key: K, value:V, failureAction: (V)-> V?):V?{
    val existentValue = get(key)
    
    if (existentValue == null) {
        put(key, value)
        return value
    } else {

        return failureAction.invoke(existentValue)
    }
}

fun <K: Any, V: Any>  MutableMap<K, V>.putOverwriting(key: K, value:V, overwrittenAction: (V)-> Unit){
    val existentValue = get(key)
    put(key, value)
      if (existentValue != null) {
          overwrittenAction.invoke(existentValue)
      }
}

inline fun <reified T> Array<out T>.flattenVarargs(): List<T> {

    return buildList {
        for (element in this@flattenVarargs) {
            when (element) {
                is List<*> -> addAll(element.filterIsInstance<T>())
                is Array<*> -> addAll(element.filterIsInstance<T>())
                else -> add(element)
            }
        }
    }
}

fun <T> List<T>.lastIndexedOrNull(action: ((Int, T) -> Unit)? = null): Pair<Int, T>? {
    val size = this.size
    val last = this.lastOrNull()
    if(last != null) {
        action?.invoke(size-1, last)
       return Pair(size-1, last)
    }else{
        return null
    }
}



fun <T> List<T>.firstIndexedOrNull(predicate: (T) -> Boolean ): Pair<Int, T>? {
    val index =  indexOfFirst(predicate)
    return if(index > -1){
        Pair(index,get(index))
    }else{
        null
    }
}

fun <T> List<T>.second(): T {
    if (size < 2) {
        throw NoSuchElementException("List is empty.")
    }
    return this[1]
}

fun <T> List<T>.third(): T {
    if (size < 3) {
        throw NoSuchElementException("List is empty.")
    }
    return this[2]
}

fun <T> List<T>.warnOverwriting(value: T, overwrittenAction: (T)-> Unit){
    val indexed = firstIndexedOrNull {
        it === value
    }
    if(indexed != null){
        overwrittenAction.invoke(indexed.second)
    }
}

@PublishedApi
internal inline fun <T> List<T>.forEachButLast(skipLastSize: Int, action: (T) -> Unit) {
    val repeatTill = this.size - skipLastSize
    for(i in 0 until repeatTill){
        val element = this[i]
        action(element)
    }
}

enum class ElementPosition { Single, First, Middle, Last }

@PublishedApi
internal inline fun <T> List<T>.forEachPositioned(action: (ElementPosition, T) -> Unit) {
    if(size == 1){
        action(ElementPosition.Single,  this[0])
        return
    }
    for(i in 0 until size){
        val element = this[i]
        if(i == 0){
            action(ElementPosition.First, element)
            continue
        }
        if(i == size - 1){
            action(ElementPosition.Last, element)
            continue
        }
        action(ElementPosition.Middle, element)
    }
}



internal inline fun <T> List<T>.onLastIfAny(action: (T) -> Unit) {
    val element = lastOrNull()
    if(element != null) action(element)
}


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




