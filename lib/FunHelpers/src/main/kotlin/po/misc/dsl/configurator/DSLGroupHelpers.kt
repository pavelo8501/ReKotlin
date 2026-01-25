package po.misc.dsl.configurator

import po.misc.interfaces.named.KeyedValue
import po.misc.interfaces.named.TextContaining


internal fun  TextContaining.generateName(): String{
   return when(val value =  this){
        is KeyedValue -> value.asText()
        else -> "Group # ${value.asText()}"
    }
}