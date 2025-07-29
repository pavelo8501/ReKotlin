package po.misc.validators


fun <T: Any, R: Any?> Validator.sequentialValidation(
    name: String,
    validatableList: List<T>,
    validatorBlock: SequentialContainer<T, R>.(validatable:(T))-> R
):  SequentialContainer<T, R> {

    val container = SequentialContainer<T, R>(name, validatableList, this)
    validatableList.forEach { item->
        container.provideValidatable(item)
        val result =  validatorBlock.invoke(container, item)
        container.provideResult(result)
    }
    validations.add(container)
    container.validationComplete()
    return container
}

/***
 * Overload for reassignment of validatable :T
 * copies everything from the parent container
 */
fun <T: Any, R: Any?> Validator.validation(
    name: String,
    validatable: T,
    validatorBlock: ValidationContainer<T, R>.(T)-> R
): ValidationContainer<T, R> {
    val container =  ValidationContainer<T, R>(name, validatable, this)

    container.provideValidatable(validatable)
    val result = validatorBlock.invoke(container, container.nowValidating)
    container.provideResult(result)
    validations.add(container)
    return container
}