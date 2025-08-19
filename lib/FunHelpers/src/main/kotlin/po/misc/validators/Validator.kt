package po.misc.validators


import po.misc.context.CTX
import po.misc.validators.models.CheckStatus
import po.misc.validators.reports.ValidationReport


class Validator(
   private val validating: Any
) {
    val validatingCTXName: String get() {
        return if (validating is CTX) {
            validating.identifiedByName
        } else {
            validating::class.simpleName.toString()
        }
    }


    val validations:  MutableList<ValidationContainerBase<*, *>> = mutableListOf()

    val reports : List<ValidationReport> get(){
        return validations.map { it.validationReport }
    }

    val overallResult: CheckStatus get() {
       val results = reports.map { it.overallResult }
       if(results.all { it ==  CheckStatus.PASSED}){
           return CheckStatus.PASSED
       }else{
           return CheckStatus.FAILED
       }
    }
    var validationPrefix: String = ""

    fun build(
        block: Validator.()-> Unit
    ): List<ValidationReport> {
        validationPrefix = "N/A"
        this.block()
        return reports
    }
}