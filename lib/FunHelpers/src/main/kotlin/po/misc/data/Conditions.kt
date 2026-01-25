package po.misc.data

/**
 * Detects whether assigning [other] would overwrite the current [value].
 *
 * This function is intentionally **non-throwing** and is designed for
 * soft-failure domains such as templating or rendering pipelines, where
 * overwrites are not catastrophic and should not interrupt execution.
 *
 * If [value] is equal to [other], [action] is invoked and the function
 * returns `true`. Otherwise, no action is performed and `false` is returned.
 *
 * Typical use cases include:
 * - Emitting warnings when the same renderable is assigned twice
 * - Detecting idempotent or redundant configuration
 * - Observational checks in builder-style APIs
 *
 * @param value the current value already assigned
 * @param other the new value being assigned
 * @param action optional side-effect executed when an overwrite is detected
 *
 * @return `true` if [value] equals [other], indicating a potential overwrite;
 *         `false` otherwise
 */
fun <T> checkOverwrites(value:T, other:T, action: (()-> Unit)? = null):Boolean {
    if(value == other) {
        action?.invoke()
        return true
    }
    return false
}

fun ifNullOrBlank(value: String?, action: (()-> Unit)? = null):Boolean {
   if(value.isNullOrBlank()){
        return false
    }
    action?.invoke()
    return true
}


fun String?.ifNotBlank(action: ((String)-> Unit)? = null):String?{
    if(isNullOrBlank()){
        return null
    }
    action?.invoke(this)
    return this
}



val <T> T.isUnset : Boolean get() {
    return when(val thisReceiver = this){
        is String -> thisReceiver.isNotBlank()
        else -> thisReceiver != null
    }
}

fun Any?.isNull(): Boolean{
    return this == null
}

fun Any?.isNotNull(): Boolean{
    return this != null
}



fun ifNull(value: Any?, action: (()-> Unit)? = null):Boolean {
    if(value == null) {
        action?.invoke()
        return true
    }
    return false
}

fun ifNotNull(value: Any?, action: (()-> Unit)? = null):Boolean {
    if(value != null) {
        action?.invoke()
        return true
    }
    return false
}

fun ifEmptyList(value: List<*>, action: (()-> Unit)? = null):Boolean {
    if(value.isEmpty()) {
        action?.invoke()
        return true
    }
    return false
}

/**
 * Checks whether the given [value] has been initialized.
 *
 * This is a **soft guard** utility intended for scenarios where missing or
 * uninitialized values should be reported or logged rather than treated
 * as fatal errors.
 *
 * If [value] is `null`, [action] is invoked and the function returns `true`.
 * If [value] is of type [List]  and empty [action]  is invoked and the function returns `true`.
 * If [value] is non-null, the function returns `false` and no action is taken.
 *
 * Typical use cases include:
 * - Validating optional template bindings
 * - Detecting missing configuration in rendering pipelines
 * - Providing user-facing warnings without aborting execution
 *
 * @param value the value to check for initialization
 * @param action optional side-effect executed when the value is uninitialized
 *
 * @return `false` if [value] is non-null (initialized); `tru` otherwise
 */
fun ifUndefined(value: Any?, action: (()-> Unit)? = null):Boolean {
   return when(value){
        is String -> { ifNullOrBlank(value, action) }
        is List<*> -> ifEmptyList(value, action)
        else -> ifNull(value, action)
    }
}


