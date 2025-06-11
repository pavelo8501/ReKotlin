package po.misc.validators.general


import po.misc.interfaces.Identifiable
import po.misc.validators.general.reports.ValidationReport


class Validator {


    val validations:  MutableList<ValidationContainerBase<*>> = mutableListOf()
    var identifiable: Identifiable? = null

    val reports : List<ValidationReport> get(){
        return validations.map { it.validationReport }
    }

    fun validate(
        validationName: String,
        identifiable: Identifiable,
        block: Validator.()-> Unit
    ): List<ValidationReport> {
        this.identifiable = identifiable
        block.invoke(this)



//        val container = ValidationContainer(identifiable, validatable, this).setValidationName(validationName).also {
//            it.validationName = validationName
//        }
//        block.invoke(container, validatable)
//        validations.add(container)
//
//
        return reports
    }
}