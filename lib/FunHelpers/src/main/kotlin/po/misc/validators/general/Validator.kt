package po.misc.validators.general


import po.misc.interfaces.IdentifiableContext
import po.misc.validators.general.reports.ValidationReport


class Validator {


    val validations:  MutableList<ValidationContainerBase<*>> = mutableListOf()
    var identifiable: IdentifiableContext? = null

    val reports : List<ValidationReport> get(){
        return validations.map { it.validationReport }
    }

    var validationPrefix: String = ""


    fun validate(
        validationName: String,
        identifiable: IdentifiableContext,
        block: Validator.()-> Unit
    ): List<ValidationReport> {
        this.identifiable = identifiable
        validationPrefix = validationName
        block.invoke(this)
        return reports
    }
}