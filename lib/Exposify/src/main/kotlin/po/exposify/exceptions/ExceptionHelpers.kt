package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException

fun  Boolean.trueOrInitException(){

    if(!this){
       val exception = InitException("Tested value is false", ExceptionCode.UNDEFINED)
        exception.setHandler(HandlerType.CANCEL_ALL)
        throw exception
    }
    return
}