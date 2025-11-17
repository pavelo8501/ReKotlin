package po.lognotify.debug.extensions

import po.lognotify.debug.interfaces.DebugProvider
import po.lognotify.debug.models.InputParameter
import po.misc.types.safeCast
import po.misc.types.token.TypeToken


internal inline fun <reified T: Any> createInputParameter(index: Int, parameter: List<T>): InputParameter {
    val list = InputParameter("Parameter_$index", TypeToken.create<List<Any>>(), parameter.size, "list")
    parameter.forEachIndexed { index, item ->
        val item = InputParameter("Item_$index", TypeToken.create<T>(), 1, item.toString())
        list.addListParameter(item)
    }
    return list
}

internal inline fun <reified T: Any> createInputParameter(index: Int, parameter:T):InputParameter{
    if(parameter is List<*>) {
        parameter.safeCast<List<Any>>()?.let {
            return createInputParameter(index, it)
        }
    }
   return InputParameter("Parameter_$index", TypeToken.create<T>(), 1, parameter.toString())
}


inline fun <reified T: Any> DebugProvider.captureProperty(parameter: List<T>):InputParameter{

    val list =  InputParameter("Parameter_0", TypeToken.create<List<Any>>(), parameter.size, "list")
    parameter.forEachIndexed { index, item->

        val item = InputParameter("Item_$index", TypeToken.create<T>(), 1, item.toString())
        list.addListParameter(item)
    }
    inputParams.add(list)
    return list
}

inline fun <reified T: Any> DebugProvider.captureProperty(parameter: T):InputParameter{
    if(parameter is List<*>){
        parameter.safeCast<List<T>>()?.let {
            return captureProperty(it)
        }
    }
    val inputParameter = InputParameter("Parameter_${inputParams.size}", TypeToken.create<T>(), 1, parameter.toString())
    inputParams.add(inputParameter)
    return inputParameter
}
