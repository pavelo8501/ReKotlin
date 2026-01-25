package po.misc.data.pretty_print.parts.options

import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.strings.appendLine
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TokenHolder
import po.misc.types.token.TypeToken
import java.util.UUID


@PublishedApi
internal interface PrettyHelper {

    fun generateRowID(type: TokenHolder,  hashCode: Int?): DefaultRowID = Companion.generateRowID(type, hashCode)
    fun toRowOptions(input: CommonRowOptions?): RowOptions = Companion.toRowOptions(input)
    fun toRowOptions(input: CommonRowOptions?, default : CommonRowOptions? = null): RowOptions = Companion.toRowOptions(input, default)
    fun toRowOptionsOrNull(input: CommonRowOptions?): RowOptions? = Companion.toRowOptionsOrNull(input)

    fun toOptions(input: PrettyOptions?, default: CellOptions? = null): Options = Companion.toOptions(input, default)
    fun toOptionsOrNull(input: PrettyOptions?): Options? = Companion.toOptionsOrNull(input)

    fun createRowData(tokenized: TokenHolder, renderableType: RenderableType,rowID: RowID?): TemplateData =
        Companion.createRowData(tokenized, renderableType, rowID)

    fun createRowData(compositionTrace: CompositionTrace): TemplateData = Companion.createRowData(compositionTrace)
    fun createGridData(compositionTrace: CompositionTrace): TemplateData = Companion.createGridData(compositionTrace)

    fun List<String>.joinRender(orientation: Orientation):String {
        val thisList = this
        return buildString {
            thisList.forEach {str->
                if(orientation == Orientation.Horizontal){
                    append(str)
                }else{
                    appendLine(str)
                }
            }
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

    internal fun generateRowID(receiverType:TokenHolder, hashCode: Int?): DefaultRowID{
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
    internal fun createRowData(tokenized: TokenHolder, renderableType: RenderableType, rowID: RowID?): TemplateData{
        return rowID?.let {
            val composition = CompositionTrace.createFrom(tokenized, renderableType, it)
            TemplateData(composition)
        }?:run {
            val composition = CompositionTrace.createFrom(tokenized, renderableType, generateRowID(tokenized, null))
            TemplateData(composition)
        }
    }
    internal fun createRowData(compositionTrace: CompositionTrace): TemplateData{
        return TemplateData(compositionTrace)
    }
    internal fun createGridData(compositionTrace: CompositionTrace): TemplateData{
        return TemplateData(compositionTrace)
    }

    fun toRowOptions(input: CommonRowOptions): RowOptions =
        when (input) {
            is RowOptions -> input
            is RowPresets -> RowOptions(input)
        }
    fun toRowOptions(input: CommonRowOptions?, default : CommonRowOptions? = null): RowOptions {
        if(input != null) {
          return toRowOptions(input)
        }
        if(default != null) {
           return toRowOptions(default)
        }
        return RowOptions(Orientation.Horizontal)
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

}