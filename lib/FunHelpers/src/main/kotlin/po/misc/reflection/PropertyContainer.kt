package po.misc.reflection

import po.misc.exceptions.throwableToText
import kotlin.reflect.KProperty


interface PropertyContainer {

    val property: KProperty<Any>
    var value: Any

    val errorRegistrator: (String)-> Unit

    fun updateValue(newValue: Any?, ex: Throwable?){
        if(newValue != null){
            value =  newValue
        }else{
            errorRegistrator.invoke(ex?.throwableToText()?:"")
        }
    }



}