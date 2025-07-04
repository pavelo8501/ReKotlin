package po.lognotify.debug.extensions

import po.lognotify.debug.interfaces.DebugProvider
import po.lognotify.debug.models.InputParameter
import po.misc.types.TypeData
import po.misc.types.safeCast


internal inline fun <reified T: Any> createInputParameter(index: Int, parameter: List<T>): InputParameter {
    val list = InputParameter("Parameter_$index", TypeData.createRecord<List<Any>>(), parameter.size, "list")
    parameter.forEachIndexed { index, item ->
        val item = InputParameter("Item_$index", TypeData.createRecord<T>(), 1, item.toString())
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
   return InputParameter("Parameter_$index", TypeData.createRecord<T>(), 1, parameter.toString())
}


inline fun <reified T: Any> DebugProvider.captureProperty(parameter: List<T>):InputParameter{
    val list =  InputParameter("Parameter_0", TypeData.createRecord<List<Any>>(), parameter.size, "list")
    parameter.forEachIndexed { index, item->
        val item = InputParameter("Item_$index", TypeData.createRecord(item::class), 1, item.toString())
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
    val inputParameter = InputParameter("Parameter_${inputParams.size}", TypeData.createRecord(parameter::class), 1, parameter.toString())
    inputParams.add(inputParameter)
    return inputParameter
}
