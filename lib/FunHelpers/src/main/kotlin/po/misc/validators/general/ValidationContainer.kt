package po.misc.validators.general

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.validators.general.reports.ReportRecord
import po.misc.validators.general.reports.ValidationReport

sealed class ValidationContainerBase<T>(
    val identifiable: Identifiable,
    var validatable: T,
    internal val validator: Validator
){
    var validationName: String = ""

    internal val validationReport: ValidationReport = ValidationReport(this.validationName)

    internal var initializerFn:(ValidationContainer<T>.(T) -> Unit)? = null
    internal var validationPredicates :MutableMap<String, (ValidationContainer<T>.(validatable:(T))-> Unit)> = mutableMapOf()

    var validatorBlocks : MutableMap<String,ValidationContainer<T>.(validatable:(T))-> Unit> = mutableMapOf()

    internal fun setValidationName(name:String): ValidationContainerBase<T>{
        validationName = name
        return this
    }

    fun <T2: Any> reassignValidatable(validatable: T2){

        this.validatable = validatable as T
    }

    internal fun dropInitializer(){
        initializerFn = null
    }

    fun initializer(initHook: ValidationContainer<T>.(T) -> Unit): ValidationReport{
        initializerFn = initHook
        return validationReport
    }
}

class ValidationContainer<T>(
    identifiable: Identifiable,
    validatable: T,
    validator: Validator
):ValidationContainerBase<T>(identifiable, validatable, validator) {
    var stop: Boolean = false
    fun stopSignal(stop: Boolean){
        this.stop  = stop
    }

    fun getReport(): ValidationReport{
        return validationReport
    }
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
            container.validationName = validationName
        }
        validatorBlock.invoke(validationContainer, validatable)
        validations.add(validationContainer)
      validationContainer
    }?:run {
      val failedValidation =   ValidationContainer(asIdentifiable("Null", "Null"), validatable, this).also {
            it.validationReport.addRecord(ReportRecord.fail(it,"Initialization", "Identifiable not found in Validator"))
        }
        validations.add(failedValidation)
        failedValidation
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
    val casted = this.castOrThrow<ValidationContainer<T>, ManagedException>()
    validatorBlock.invoke(casted, validatable)
    validator.validations.add(casted)
    return casted
}


