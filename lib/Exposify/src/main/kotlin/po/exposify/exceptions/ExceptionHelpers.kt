package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManageableException

fun  Boolean.trueOrInitException(){
    if(!this){
       val exception = InitException("Tested value is false", ExceptionCode.UNDEFINED, null)
        throw exception
    }
    return
}

fun initException(message: String, code: ExceptionCode): InitException{
    return ManageableException.build<InitException>(message, code)
}

fun operationsException(message: String, code: ExceptionCode): InitException{
    return ManageableException.build<InitException>(message, code)
}

fun throwInit(message: String, code: ExceptionCode): Nothing{
    val exception = ManageableException.build<InitException>(message, code)
    throw exception
}

fun throwOperations(message: String, code: ExceptionCode): Nothing{
    val exception = ManageableException.build<OperationsException>(message, code)
    throw exception
}
