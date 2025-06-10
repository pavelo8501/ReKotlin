package po.misc.validators.general


class Validator {

    fun <T : Any> executeCheck(
        container: ValidatableContainer<T>,
        predicate: ValidatableContainer<T>.(()->T) -> Unit
    ){
        container.runCheck("Some Message", predicate)
    }
}