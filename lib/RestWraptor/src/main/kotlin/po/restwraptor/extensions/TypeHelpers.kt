package po.restwraptor.extensions


import po.misc.exceptions.HandlerType
import po.misc.exceptions.castOrException
import po.misc.exceptions.getOrException
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.DataException
import po.restwraptor.exceptions.ExceptionCodes

//
//inline fun <reified T: Any> Any?.castOrThrow(
//    message: String,
//    code: ExceptionCodes,
//    handlerType : HandlerType = HandlerType.CANCEL_ALL): T
//{
//   return  this.castOrException {
//       ConfigurationException(message, code, handlerType)
//   }
//}


//internal inline fun <reified T: Any> T?.getOrConfigurationEx(
//    message: String,
//    code: ExceptionCodes,
//    handlerType : HandlerType = HandlerType.CANCEL_ALL): T{
//    return  this.getOrException(){
//       throw ConfigurationException(message, code, handlerType)
//    }
//}

//internal inline fun <reified T: Any> T?.getOrDataEx(
//    message: String,
//    code: ExceptionCodes,
//    handlerType : HandlerType = HandlerType.SKIP_SELF): T{
//    return  this.getOrException{
//       throw DataException(message, code, handlerType)
//    }
//}

