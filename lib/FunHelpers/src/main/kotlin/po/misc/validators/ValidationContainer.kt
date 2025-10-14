package po.misc.validators

import po.misc.types.getOrManaged
import po.misc.validators.components.BaseHooks
import po.misc.validators.components.SequentialHooks
import po.misc.validators.components.ValidatorHooks
import po.misc.validators.models.CheckStatus
import po.misc.validators.reports.ValidationReport
import kotlin.reflect.KClass

sealed class ValidationContainerBase<T: Any?, R: Any?>(
    val validationName: String,
    internal val validator: Validator
) {

    private var nowValidatingKClass: KClass<*>? = null

    private var validatableBacking:T? = null
    @PublishedApi
    internal open val validatable:T? = null

    val nowValidating:T  get() {
       return validatableBacking?:validatable.getOrManaged(this, nowValidatingKClass?: Any::class)
    }

    internal var lastResult:R? = null
        private set

    internal fun provideResult(result:R?){
        lastResult = result
    }

    internal fun provideValidatable(record:T){
        if(nowValidatingKClass == null){
            if(record != null){
                nowValidatingKClass = record::class
            }
        }
        validatableBacking = record
    }

    internal open val hooks : ValidatorHooks<T> = BaseHooks()
    val overallStatus: CheckStatus get() = validationReport.overallResult

    @PublishedApi
    internal val validationReport: ValidationReport = ValidationReport(validator.validatingCTXName, validationName)

    internal fun validationComplete(): ValidationContainerBase<T, R>{
        if(overallStatus == CheckStatus.PASSED){
            hooks.validationSuccess?.invoke(overallStatus)
        }else{
            hooks.validationFailure?.invoke(overallStatus)
        }
        return  this
    }
}

class ValidationContainer<T: Any, R: Any?>(
    validationName: String,
    override val validatable: T,
    validator: Validator
):ValidationContainerBase<T, R>(validationName, validator) {

}

class SequentialContainer<T: Any, R: Any?>(
    validationName: String,
    validatableList: List<T>,
    validator: Validator
):ValidationContainerBase<T, R>(validationName, validator) {

    val sequentialHooks: SequentialHooks<T, R> = SequentialHooks()

}



