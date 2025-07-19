package po.misc.validators.general


import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.validators.general.reports.ValidationReport


class Validator {


    val validations:  MutableList<ValidationContainerBase<*>> = mutableListOf()
    var identifiable: CTX? = null

    val reports : List<ValidationReport> get(){
        return validations.map { it.validationReport }
    }

    var validationPrefix: String = ""


    fun validate(
        validationName: String,
        identifiable: CTX,
        block: Validator.()-> Unit
    ): List<ValidationReport> {
        this.identifiable = identifiable
        validationPrefix = validationName
        block.invoke(this)
        return reports
    }
}