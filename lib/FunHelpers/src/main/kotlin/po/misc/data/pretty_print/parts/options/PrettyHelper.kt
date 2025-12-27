package po.misc.data.pretty_print.parts.options

import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.template.DefaultID
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.parts.template.TemplateData
import po.misc.types.token.TypeToken
import java.util.UUID


@PublishedApi
internal interface PrettyHelper {


    fun generateRowID(type: TypeToken<*>,  hashCode: Int?): DefaultID = Companion.generateRowID(type, hashCode)
    fun toRowOptions(input: CommonRowOptions?): RowOptions = Companion.toRowOptions(input)
    fun toRowOptions(input: CommonRowOptions?, default : CommonRowOptions? = null): RowOptions = Companion.toRowOptions(input, default)
    fun toRowOptionsOrNull(input: CommonRowOptions?): RowOptions? = Companion.toRowOptionsOrNull(input)

    fun toOptions(input: PrettyOptions?, default: CellOptions? = null): Options = Companion.toOptions(input, default)
    fun toOptionsOrNull(input: PrettyOptions?): Options? = Companion.toOptionsOrNull(input)
    fun createTemplateData(gridID: RowID?, receiverType: TypeToken<*>): TemplateData =
        Companion.createTemplateData(gridID, receiverType)

    fun createTemplateData(gridID: GridID?, dataProvider: DataProvider<*, *>, type: RenderableType): TemplateData =
        Companion.createTemplateData(gridID, dataProvider, type)

    companion object : PrettyHelperClass()
}

open class PrettyHelperClass{

    fun toRowOptions(input: CommonRowOptions): RowOptions =
        when (input) {
            is RowOptions -> input
            is RowPresets ->{
                RowOptions(input)
            }
        }
    fun toRowOptions(input: CommonRowOptions?, default : CommonRowOptions? = null): RowOptions {
        val options =  if(default == null){
            input?.let(::toRowOptions) ?: run {
                RowOptions(Orientation.Horizontal)
            }
        }else{
            when(default){
                is RowOptions ->{
                    if(default.sealed){
                        default
                    }else{
                        input?.let(::toRowOptions) ?: run { default }
                    }
                }
                is RowPresets ->{
                    input?.let(::toRowOptions) ?: run {
                        toRowOptions(default)
                    }
                }
            }
        }
        return options
    }
    fun toRowOptionsOrNull(input: CommonRowOptions?): RowOptions?{
        if(input == null){
            return null
        }
        return toRowOptions(input)
    }

    fun toOptions(input: PrettyOptions): Options =
        when (input) {
            is Options ->  input
            else -> input.asOptions()
        }
    fun toOptions(input: PrettyOptions?, default: CellOptions? = null): Options {
        if(input != null){
            return toOptions(input)
        }
        if(default != null){
            return toOptions(default)
        }
        return Options()
    }
    fun toOptionsOrNull(input: PrettyOptions?): Options? {
        if(input == null){
            return null
        }
        return toOptions(input)
    }

    internal fun generateRowID(receiverType: TypeToken<*>, hashCode: Int?): DefaultID{
        val useHash = hashCode?:UUID.randomUUID().hashCode()
        return DefaultID(receiverType.typeName, RenderableType.Row, useHash)
    }

    internal fun generateGridID(dataProvider: DataProvider<*, *>, type: RenderableType): DefaultID{
        return DefaultID("${dataProvider.typeToken.typeName},${dataProvider.valueType}",type, dataProvider.hashCode())
    }

    internal fun generateGridID(receiverType: TypeToken<*>, hashCode: Int?): DefaultID{
        val useHash = hashCode?:UUID.randomUUID().hashCode()
        return DefaultID(receiverType.typeName, RenderableType.Grid, useHash)
    }

    internal fun createTemplateData(rowID: RowID?, receiverType: TypeToken<*>): TemplateData{
        return rowID?.let {
            TemplateData(it, receiverType.typeName)
        }?:run {
           val genericRowID =  generateRowID(receiverType, null)
           TemplateData(genericRowID)
        }
    }

    internal fun createTemplateData(gridID: GridID?, dataProvider: DataProvider<*, *>, type: RenderableType): TemplateData{
        return gridID?.let {
            TemplateData(it, "${dataProvider.typeToken.typeName},${dataProvider.valueType}")
        }?:run {
            val genericGridID =  generateGridID(dataProvider, type)
            TemplateData(genericGridID)
        }
    }

    internal fun createGridData(gridID: GridID?, receiverType: TypeToken<*>, hashCode: Int? = null): TemplateData{
        return gridID?.let {
            TemplateData(it, receiverType.typeName)
        }?:run {
            val genericGridID =  generateGridID(receiverType, hashCode)
            TemplateData(genericGridID)
        }
    }

}