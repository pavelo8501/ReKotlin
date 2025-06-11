package po.misc.validators.general.validators

import po.misc.data.helpers.emptyOnNull
import po.misc.validators.general.ValidationContainer
import po.misc.validators.general.reports.ReportRecord

fun <T: Any> ValidationContainer<T>.conditionTrue(
    checkName: String,
    failureMessage: String?= null,
    predicate: (T)-> Boolean
): ReportRecord {

  return  try {
        val result = predicate.invoke(validatable)
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


