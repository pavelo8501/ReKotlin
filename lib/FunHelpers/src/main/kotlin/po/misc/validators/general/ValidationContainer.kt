package po.misc.validators.general

import po.misc.context.CTX
import po.misc.types.castOrManaged
import po.misc.validators.general.models.CheckStatus
import po.misc.validators.general.reports.ReportRecord
import po.misc.validators.general.reports.ValidationReport
import po.misc.validators.general.validators.ValidatorHooks

sealed class ValidationContainerBase<T: Any>(
    val identifiable: CTX,
    var validatable: T,
    internal val validator: Validator
) {
    var validationName: String = ""

    internal open val hooks : ValidatorHooks<T> = ValidatorHooks()

    val overallStatus: CheckStatus get() = validationReport.overallResult

    internal val validationReport: ValidationReport = ValidationReport(identifiable, validationName)
    internal fun setValidationName(name: String): ValidationContainerBase<T> {
        validationName = name
        validationReport.setName(name)
        return this
    }

    internal fun validationComplete(){
        hooks.onResultCallback?.invoke(overallStatus)
    }

    internal fun <T2 : Any> reassignValidatable(validatable: T2) {
        this.validatable = validatable as T
    }

}

class ValidationContainer<T: Any>(
    identifiable: CTX,
    validatable: T,
    validator: Validator
):ValidationContainerBase<T>(identifiable, validatable, validator) {



}

class SequentialContainer<T: Any>(
    identifiable: CTX,
    validatable: List<T>,
    validator: Validator
):ValidationContainerBase<List<T>>(identifiable, validatable, validator) {

    val sequentialHooks: ValidatorHooks<T> = ValidatorHooks()

}


/***
 * Creates new validation container i.e. ValidationReport
 */

fun <T: Any> Validator.validation(
    validationName: String,
    validatable: T,
    validatorBlock: ValidationContainer<T>.(validatable:(T))-> Unit
): ValidationContainer<T> {

  return  identifiable?.let {
        val validationContainer = ValidationContainer(it, validatable, this).also {container->
            container.setValidationName("$validationPrefix | $validationName")
        }
        validatorBlock.invoke(validationContainer, validatable)
        validations.add(validationContainer)
      validationContainer
    }?:run {


//       val failedValidation =   ValidationContainer(identifiable("Null", identifiable), validatable, this).also {
//            it.validationReport.addRecord(ReportRecord.fail(it,"Initialization", "Identifiable not found in Validator"))
//        }
//        validations.add(failedValidation)
//        failedValidation

        TODO("Depr")
    }
}

fun <T: Any> Validator.sequentialValidation(
    validationName: String,
    validatableList: List<T>,
    validatorBlock: SequentialContainer<T>.(validatable:(T))-> ReportRecord
):  SequentialContainer<T> {

    return identifiable?.let {
        val validationContainer = SequentialContainer(it, validatableList, this).also { container ->
            container.setValidationName("$validationPrefix | $validationName")
        }
        validatableList.forEach { validatable ->
            val reportRecord = validatorBlock.invoke(validationContainer, validatable)
            validationContainer.validationReport.addRecord(reportRecord)
        }
        validations.add(validationContainer)
        validationContainer.validationComplete()
        validationContainer
    } ?: run {
//        val failedValidation = SequentialContainer(asIdentifiable("Null", "Null"), validatableList, this).also {
//            it.validationReport.addRecord(
//                ReportRecord.fail(
//                    it,
//                    "Initialization",
//                    "Identifiable not found in Validator"
//                )
//            )
//
//        }
//        validations.add(failedValidation)
//        failedValidation.validationComplete()
//        failedValidation
        TODO("Depr")
    }
}

/***
 * Overload for reassignment of validatable :T
 * copies everything from the parent container
 */
fun <T: Any> ValidationContainerBase<*>.validation(
    validatable: T,
    validatorBlock: ValidationContainer<T>.(validatable:(T))-> Unit
): ValidationContainer<T> {
    reassignValidatable(validatable)
    val casted = this.castOrManaged<ValidationContainer<T>>(this)
    validatorBlock.invoke(casted, validatable)
    validator.validations.add(casted)
    return casted
}


