package po.lognotify.debug.models

import po.misc.data.styles.SpecialChars
import po.misc.types.type_data.TypeData

data class InputParameter(
    val name: String,
    val typeData: TypeData<*>,
    val size:Int = 1,
    val value: String
){
    val items: MutableList<InputParameter> = mutableListOf()

    fun addListParameter(parameter: InputParameter){
        items.add(parameter)
    }

    override fun toString(): String {

       val paramValue = if(items.isNotEmpty()){
            items.joinToString(prefix = SpecialChars.NEW_LINE) { it.toString() }
        }else{
           value
       }
       return "Name:$name:${typeData.simpleName} = $paramValue"
    }
}
