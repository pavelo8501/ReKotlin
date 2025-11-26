package po.misc.validators.validators

import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.exceptions.throwableToText
import po.misc.types.getOrManaged
import po.misc.validators.SequentialContainer
import po.misc.validators.ValidationContainer
import po.misc.validators.ValidationContainerBase
import po.misc.validators.reports.ValidationRecord


@PublishedApi
internal fun <T: Any,  R: Any?> notifySuccess(
    result:R,
    container: ValidationContainerBase<T, R>
):R{

    when(container){
        is SequentialContainer -> {
            container.sequentialHooks.conditionSuccess?.invoke(result)
        }
        is ValidationContainer<*, *> -> {

        }
    }
    return result
}

@PublishedApi
internal fun <T: Any> notifyFail(
    recordProcessing:T,
    container: ValidationContainerBase<T, *>
){

    when(container){
        is SequentialContainer -> {
            container.sequentialHooks.conditionFailure?.invoke(recordProcessing)
        }
        is ValidationContainer<*, *> -> {

        }
    }
}

@PublishedApi
internal fun <T: Any> notifyException(
    exception: Throwable,
    recordProcessing:T,
    container: ValidationContainerBase<T, *>
){

    when(container){
        is SequentialContainer -> {
            container.sequentialHooks.failure?.invoke(exception, recordProcessing)
        }
        is ValidationContainer<*, *> -> {

        }
    }
}




fun <T: Any> ValidationContainerBase<T, Boolean>.conditionTrue(
    checkName: String,
    failureMessage: String?= null,
    predicate: (T)-> Boolean,
): Boolean {

  return  try {
        val result = predicate.invoke(nowValidating)

        if(result){
            validationReport.addRecord(ValidationRecord.success(this,checkName))
            notifySuccess(true, this)
        }else{
            validationReport.addRecord(ValidationRecord.fail(this, checkName, failureMessage.orDefault()))
            notifyFail(nowValidating, this)
            false
        }
    }catch (ex: Throwable){
      validationReport.addRecord(ValidationRecord.fail(this, checkName, ex))
      notifyException(ex, nowValidating, this)
      false
    }
}

fun <T: Any, R> ValidationContainerBase<T, R>.conditionNotNull(
    checkName: String,
    failureMessage: String?= null,
    predicate: (T)-> R?,
):R? {

    return  try {
        predicate.invoke(nowValidating)?.let {result->
            validationReport.addRecord(ValidationRecord.success(this, checkName))
            notifySuccess<T, R>(result, this)
        }?:run {
            validationReport.addRecord(ValidationRecord.fail(this, checkName, failureMessage.orDefault()))
            notifyFail(nowValidating, this)
            null
        }
    }catch (ex: Throwable){
        validationReport.addRecord(ValidationRecord.fail(this, checkName, failureMessage?:ex.throwableToText()))
        notifyException(ex, nowValidating, this)
        return null
    }
}


inline fun <reified T: Any, R> ValidationContainerBase<T, R>.doesNotThrow(
    checkName: String,
    failureMessage: String? = null,
    noinline  block: (T)-> R,
): R? {
    return  try {
        val validating = validatable.getOrManaged(this)

        block.invoke(validating)?.let {result->
            notifySuccess<T, R>(result, this)
            validationReport.addRecord(ValidationRecord.success(this,checkName))
            result
        }?:run {
            validationReport.addRecord(ValidationRecord.success(this,checkName))
            notifyFail(nowValidating, this)
            null
        }

    }catch (ex: Throwable){
        validationReport.addRecord(ValidationRecord.fail(this, checkName, failureMessage?:ex.throwableToText()))
        notifyException(ex, nowValidating, this)
        return null
    }
}


fun <T : Any> ValidationContainer<T, Boolean>.conditionAnyTrue(
    collection: Collection<T>,
    checkName: String,
    failureMessage: String? = null,
    predicate: (T) -> Boolean
): Boolean {
    return try {
        val result = collection.any(predicate)
        if (result) {
            validationReport.addRecord(ValidationRecord.success(this, checkName))
            notifySuccess<T, Boolean>(true, this)
            true
        } else {
            validationReport.addRecord(ValidationRecord.fail(this, checkName, failureMessage.orDefault()))
            notifyFail<T>(nowValidating, this)
            false
        }
    } catch (ex: Throwable) {
        validationReport.addRecord(ValidationRecord.fail(this, checkName, ex))
        notifyException(ex, nowValidating, this)
        return false
    }
}


