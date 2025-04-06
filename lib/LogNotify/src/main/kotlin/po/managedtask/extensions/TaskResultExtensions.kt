package po.managedtask.extensions

import po.managedtask.classes.ManagedResult


inline fun <R> ManagedResult<R>.onResult(action: (value: R) -> Unit): ManagedResult<R> {

    resultContext?.let {
        action(this@onResult.value!!)
    }

    if(isSuccess  && value != null){
        action(value!!)
    }
    return this
}

