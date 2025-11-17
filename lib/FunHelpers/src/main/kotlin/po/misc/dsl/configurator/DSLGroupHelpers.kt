package po.misc.dsl.configurator

import po.misc.data.HasNameValue
import po.misc.data.TextContaining


internal fun  TextContaining.generateName(): String{
   return when(val value =  this){
        is HasNameValue -> value.asText()
        else -> "Group # ${value.asText()}"
    }
}