package po.misc.dsl.configurator


import po.misc.data.KeyedValue
import po.misc.data.TextContaining


internal fun  TextContaining.generateName(): String{
   return when(val value =  this){
        is KeyedValue -> value.asText()
        else -> "Group # ${value.asText()}"
    }
}