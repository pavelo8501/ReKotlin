package po.misc.types.token



interface TokenFactory


inline fun <reified T: Any> TokenFactory.typeToken(): TypeToken<T>{
   return TypeToken.create<T>()
}