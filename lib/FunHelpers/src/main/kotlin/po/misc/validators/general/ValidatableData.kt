package po.misc.validators.general

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.validators.mapping.reports.ReportRecord

class ValidatableContainer<T: Any>(
    val component: Identifiable,
    val instanceProvider: ()->T
) {

  //  var instanceCheckPredicate: (ValidatableContainer<T>.(validatableInstance: T) -> Unit)? = null

    private fun instanceCheck(
        failureMessage: String = "",
        predicate: ValidatableContainer<T>.(instanceProvider:T) -> Unit
    ){

      //  val report: MutableList<ReportRecord> = mutableListOf()
       // predicate.invoke(this, instanceProvider)
    }

    fun runCheck(failureMessage: String,  predicate: ValidatableContainer<T>.(instanceProvider: ()->T) -> Unit){
        predicate.invoke(this, instanceProvider)
    }

}

