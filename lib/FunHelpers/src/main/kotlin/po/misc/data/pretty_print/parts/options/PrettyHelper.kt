package po.misc.data.pretty_print.parts.options

import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken
import java.util.UUID


@PublishedApi
internal interface PrettyHelper {

    fun generateRowID(type: TypeToken<*>,  hashCode: Int?): DefaultRowID = Companion.generateRowID(type, hashCode)
    fun toRowOptions(input: CommonRowOptions?): RowOptions = Companion.toRowOptions(input)
    fun toRowOptions(input: CommonRowOptions?, default : CommonRowOptions? = null): RowOptions = Companion.toRowOptions(input, default)
    fun toRowOptionsOrNull(input: CommonRowOptions?): RowOptions? = Companion.toRowOptionsOrNull(input)

    fun toOptions(input: PrettyOptions?, default: CellOptions? = null): Options = Companion.toOptions(input, default)
    fun toOptionsOrNull(input: PrettyOptions?): Options? = Companion.toOptionsOrNull(input)

    fun createRowData(renderableType: RenderableType,rowID: RowID?,  sourceType: TypeToken<*>, receiverType: TypeToken<*>?, ): TemplateData =
        Companion.createRowData(renderableType, rowID, sourceType, receiverType)
    fun createRowData(compositionTrace: CompositionTrace): TemplateData = Companion.createRowData(compositionTrace)
    fun createGridData(compositionTrace: CompositionTrace): TemplateData = Companion.createGridData(compositionTrace)

    fun List<String>.joinRender(orientation: Orientation):String {
        return if(orientation == Orientation.Horizontal){
            joinToString(separator = SpecialChars.EMPTY)
        }else{
            joinToString(separator = SpecialChars.NEW_LINE)
        }
    }

    fun Array<*>.flattenVarargs(): List<Any> {
        val flattened  = mutableListOf<Any>()
        fun checkNullMakeString(element: Any?): Any {
            return element ?: "null" as Any
        }
        for (i in 0 until size) {
            val element = get(i)
            if(element is List<*>){
                val result =  element.map {
                    checkNullMakeString(it)
                }
                flattened.addAll(result)
            }else {
                flattened.add(checkNullMakeString(element))
            }
        }
        return flattened
    }
    fun Array<*>.flattenVarargs(firstValue: Any): List<Any> {
        val flattened  = mutableListOf(firstValue)
        fun checkNullMakeString(element: Any?): Any {
            return element ?: "null" as Any
        }
        for (i in 0 until size) {
            val element = get(i)
            if(element is List<*>){
                val result =  element.map {
                    checkNullMakeString(it)
                }
                flattened.addAll(result)
            }else {
                flattened.add(checkNullMakeString(element))
            }
        }
        return flattened
    }

    fun String.repeat(times: Int): String {
        val result = mutableListOf<String>()
        for(i in 0 until times) {
            result.add(this)
        }
        return result.joinToString(separator = SpecialChars.EMPTY)
    }

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
    internal fun generateRowID(receiverType: TypeToken<*>, hashCode: Int?): DefaultRowID{
        val useHash = hashCode?:UUID.randomUUID().hashCode()
        return DefaultRowID(receiverType.typeName, useHash)
    }

    internal fun generateRowID(typeString: String, hashCode: Int?): DefaultRowID{
        val useHash = hashCode?:UUID.randomUUID().hashCode()
        return DefaultRowID(typeString, useHash)
    }

    internal fun generateGridID(renderableType: RenderableType,  typeString: String): DefaultGridID{
        val useHash = UUID.randomUUID().hashCode()
        return DefaultGridID(renderableType, typeString,  useHash)
    }

    internal fun createRowData(renderableType: RenderableType,rowID: RowID?,  sourceType: TypeToken<*>, receiverType: TypeToken<*>?, ): TemplateData{
        val composition = CompositionTrace.createFrom(sourceType, receiverType, renderableType, rowID)
        return rowID?.let {
            TemplateData(it, composition)
        }?:run {
           val genericRowID = generateRowID(composition.typeString, null)
           TemplateData(genericRowID, composition)
        }
    }
    internal fun createRowData(compositionTrace: CompositionTrace): TemplateData{
        val templateID = compositionTrace.templateID
        return TemplateData(templateID,  compositionTrace)
    }
    internal fun createGridData(compositionTrace: CompositionTrace): TemplateData{
        val templateID = compositionTrace.templateID
        return TemplateData(templateID,  compositionTrace)
    }
}