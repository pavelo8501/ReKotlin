package po.misc.validators.general.validators

import po.misc.data.helpers.emptyOnNull
import po.misc.validators.general.SequentialContainer
import po.misc.validators.general.ValidationContainer
import po.misc.validators.general.ValidationContainerBase
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.reports.ReportRecord


class ValidatorHooks<T: Any>(){

    private  var onSuccess: ((T)-> Unit)? = null
    private  var onFailure: ((T)-> Unit)? = null
    internal var onResultCallback: ((CheckStatus)-> Unit)? = null

    fun onSuccess(hook:(T)-> Unit){
        onSuccess = hook
    }

    fun onFailure(hook:(T)-> Unit){
        onFailure = hook
    }

    fun onResult(hook:(CheckStatus)-> Unit){
        onResultCallback = hook
    }
}


fun <T: Any> ValidationContainer<T>.validatorHooks(
    block: ValidatorHooks<T>.()-> Unit
):ValidationContainer<T>{
    block.invoke(hooks)
    return this
}

fun <T: Any>  SequentialContainer<T>.validatorHooks(
    block: ValidatorHooks<T>.()-> Unit
):SequentialContainer<T>{

    block.invoke(sequentialHooks)
    return this
}




fun <T: Any> ValidationContainerBase<T>.conditionTrue(
    checkName: String,
    failureMessage: String?= null,
    predicate: ()-> Boolean,
): ReportRecord {
  return  try {
        val result = predicate.invoke()
        if(result){
            validationReport.addRecord(ReportRecord.success(this,checkName))
        }else{
            validationReport.addRecord(ReportRecord.fail(this, checkName, failureMessage.emptyOnNull()))
        }
    }catch (ex: Throwable){
      return validationReport.addRecord(ReportRecord.fail(this, checkName, ex))
    }
}

fun <T : Any, E> ValidationContainer<T>.conditionAnyTrue(
    collection: Collection<E>,
    checkName: String,
    failureMessage: String? = null,
    predicate: (E) -> Boolean
): ReportRecord {
    return try {
        val result = collection.any(predicate)
        if (result) {
            validationReport.addRecord(ReportRecord.success(this, checkName))
        } else {
            validationReport.addRecord(ReportRecord.fail(this, checkName, failureMessage.emptyOnNull()))
        }
    } catch (ex: Throwable) {
        validationReport.addRecord(ReportRecord.fail(this, checkName, ex))
    }
}

fun <T : Any, E> ValidationContainer<T>.conditionAllTrue(
    collection: Collection<E>,
    checkName: String,
    failureMessage: ((E) -> String)? = null,
    predicate: (E) -> Boolean
): List<ReportRecord> {
    return collection.mapIndexed { index, item ->
        try {
            if (predicate(item)) {
                validationReport.addRecord(
                    ReportRecord.success(this, "$checkName #$index")
                )
            } else {
                validationReport.addRecord(
                    ReportRecord.fail(
                        this,
                        "$checkName #$index",
                        failureMessage?.invoke(item).emptyOnNull()
                    )
                )
            }
        } catch (ex: Throwable) {
            validationReport.addRecord(
                ReportRecord.fail(this, "$checkName #$index", ex)
            )
        }
    }
}


