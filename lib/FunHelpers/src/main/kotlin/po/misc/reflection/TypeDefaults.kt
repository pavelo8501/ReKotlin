package po.misc.reflection

import po.misc.types.safeCast
import java.time.LocalDateTime
import kotlin.reflect.KClass


interface TypeDefaults{

    fun  defaultForClass(kClass: KClass<*>): Any? {
      val result =  when (kClass) {
            Int::class -> -1
            String::class -> "Default"
            Boolean::class -> false
            Long::class -> -1L
            LocalDateTime::class -> {
                LocalDateTime.now()
            }
            else -> null
        }
        return result
    }

//    fun <T: Any> getDefaultForType(typeData: TypeToken<T>): T? {
//
//        val result = when (typeData.kType.classifier) {
//            Int::class -> -1
//            String::class -> "Default"
//            Boolean::class -> false
//            Long::class -> -1L
//            LocalDateTime::class -> {
//                LocalDateTime.now()
//            }
//            else -> null
//        }
//        return  result?.safeCast(typeData.kClass)
//    }
//
//    fun getDefault(): T?{
//      return  getDefaultForType(typeData)
//    }
}

fun <T: Any>  TypeDefaults.defaultForType(kClass : KClass<T>):T?{
    val result =  defaultForClass(kClass)
    return result?.safeCast(kClass)
}