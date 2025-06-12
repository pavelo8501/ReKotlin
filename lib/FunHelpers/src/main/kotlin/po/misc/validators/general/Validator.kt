package po.misc.validators.general


import po.misc.interfaces.Identifiable
import po.misc.validators.general.reports.ValidationReport


class Validator {


    val validations:  MutableList<ValidationContainerBase<*>> = mutableListOf()
    var identifiable: Identifiable? = null

    val reports : List<ValidationReport> get(){
        return validations.map { it.validationReport }
    }

    var validationPrefix: String = ""


    fun validate(
        validationName: String,
        identifiable: Identifiable,
        block: Validator.()-> Unit
    ): List<ValidationReport> {
        this.identifiable = identifiable
        validationPrefix = validationName
        block.invoke(this)
        return reports
    }
}