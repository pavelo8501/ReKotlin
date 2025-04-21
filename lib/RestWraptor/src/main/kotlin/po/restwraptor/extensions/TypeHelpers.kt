package po.restwraptor.extensions

import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.extensions.castOrException
import po.lognotify.extensions.getOrException
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.DataException
import po.restwraptor.exceptions.ExceptionCodes


inline fun <reified T: Any> Any.castOrConfigurationEx(
    message: String,
    code: ExceptionCodes,
    handlerType : HandlerType = HandlerType.CANCEL_ALL): T
{
   return  this.castOrException(ConfigurationException(message, code, handlerType))
}

fun <T: Any> T?.getOrConfigurationEx(
    message: String,
    code: ExceptionCodes,
    handlerType : HandlerType = HandlerType.CANCEL_ALL): T{
    return  this.getOrException(ConfigurationException(message, code, handlerType))
}

fun <T: Any> T?.getOrDataEx(
    message: String,
    code: ExceptionCodes,
    handlerType : HandlerType = HandlerType.SKIP_SELF): T{
    return  this.getOrException(DataException(message, code, handlerType))
}



//
//inline fun <T: Any> T?.letOrException(ex : ManagedException, block: (T)-> T){
//    if(this != null){
//        block(this)
//    } else {
//        throw ex
//    }
//}
//
//fun <T: Any?, E: ManagedException> T.testOrException( exception : E, predicate: (T) -> Boolean): T{
//    if (predicate(this)){
//        return this
//    }else{
//        throw exception
//    }
//}