package po.misc.data.output

import po.misc.collections.forEachButLast
import po.misc.collections.onLastIfAny
import po.misc.data.Postfix
import po.misc.data.Prefix
import po.misc.data.Separator
import po.misc.data.StringModifyParams
import po.misc.data.TextWrapper
import po.misc.data.logging.Topic
import po.misc.data.logging.Verbosity
import po.misc.data.strings.ListOptions
import po.misc.data.strings.appendStyledLine
import po.misc.data.strings.stringification
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.TextSpan
import po.misc.debugging.ClassResolver
import po.misc.debugging.DebugTopic
import po.misc.interfaces.named.Named
import po.misc.interfaces.named.NamedComponent
import po.misc.time.TimeFormat
import po.misc.time.TimeHelper
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

sealed class OutputBase<R>(
   header: Any?
): TextStyler, TimeHelper {

    val outputParameters: MutableList<OutputParameter> = mutableListOf()
    val headerModifier: StringModifyParams = StringModifyParams(Separator(), Prefix(), Postfix(SpecialChars.NEW_LINE))
    val contentModifier: StringModifyParams = StringModifyParams(Separator(), Prefix(), Postfix(SpecialChars.NEW_LINE))

    var headerText:String? = null
        private set

    var preserveAnsi: Boolean = false

    init {
        header?.let {
            if (it is OutputParameter) {
                outputParameters.add(it)
            } else {
                headerText = when (it) {
                    is String -> it.style(TextStyler.ActionTag.Speech).styled
                    is TextSpan -> it.styled
                    else -> it.stringify().styled
                }
            }
        }
    }

    protected fun parameterToText(parameter: OutputParameter, separate: Boolean): String {
        val text = when (parameter) {
            is Time -> nowTime(TimeFormat).style(TextStyler.ValueTag.Time).styled
            is NameHeader -> parameter.name.style(TextStyler.ValueTag.ClassName).style(parameter.actionTag).styled
        }
        if (separate) {
            return text + headerModifier.separator
        }
        return text
    }

    protected fun getParametersText(): String {
        if(outputParameters.isEmpty()){
            return ""
        }
        val stringBuilder = StringBuilder()
        outputParameters.forEachButLast(1){ parameter ->
            val text = parameterToText(parameter, true)
            stringBuilder.append(text)
        }
        outputParameters.onLastIfAny {
            val text = parameterToText(it, false)
            stringBuilder.append(text)
        }
        return  headerModifier.wrap(stringBuilder.toString())
    }

    protected fun getText(content: Any?, styleCode: StyleCode?): String{
        if(content is TextSpan){
            return  content.styled
        }
        return content.stringify(styleCode).styled
    }

    protected fun Any?.printNotEmpty(textWrapper: TextWrapper? = null){
        if (this != null){
            val text = this.toString()
            if(text.isNotEmpty()){
                val wrapped = textWrapper?.wrap(text)?:text
                print(wrapped)
            }
        }
    }

    fun setModifier(stringModifier: TextWrapper, header: Boolean = true):OutputBase<R>{
        if(header){
            headerModifier.initialize(stringModifier)
        }else{
            contentModifier.initialize(stringModifier)
        }
        return this
    }

    fun setParameters(parameters: Array<out OutputParameter>):OutputBase<R>{
        outputParameters.addAll(parameters)
        return this
    }

    fun printAll(contentText: String){
        val parametersText = getParametersText()
        parametersText.printNotEmpty()
        val wrapped = contentModifier.wrap(contentText)
        print(wrapped)
    }

    open fun printAll(content: R? = null, styleCode: StyleCode? = null){
        val parametersText = getParametersText()
        parametersText.printNotEmpty()
        val content = if(content !is Unit){
            getText(content, styleCode)
        }else{
            getText(content, styleCode)
        }
        print(content)
    }
}

internal class Output(
    header: OutputParameter? = null,
    val content: Any?,
    val styleCode: StyleCode? = null
):OutputBase<Unit>(header){

    constructor(
        header: TextSpan,
        content: Any?,
        styleCode: StyleCode? = null
    ):this(NameHeader(header), content, styleCode)

    constructor(
        header: OutputParameter? = null,
        builderAction: StringBuilder.() -> Unit
    ):this(header, null, null) {
        stringBuilderAction = builderAction
    }
    private var stringBuilderAction: (StringBuilder.() -> Unit)? = null

    private fun textFromAction():String? {
       return stringBuilderAction?.let {action->
           val builder = StringBuilder()
           action.invoke(builder).toString()
        }
    }
    override fun printAll(content: Unit? , styleCode: StyleCode? ){
        val contentText = textFromAction()?: run {
            getText(this.content, styleCode)
        }
        printAll(contentText)
    }
}

class OutputCompare<out T>(
    header:OutputParameter? = null,
    val object1:T,
    val object2:T,
): OutputBase<Unit>(header){

    fun stringifyContent(): String{
        val meta1 = ClassResolver.instanceMeta(object1)
        val meta2 = ClassResolver.instanceMeta(object2)
        meta1.compareParameters(meta2)
        return buildString {
            appendStyledLine(meta1)
            appendStyledLine(meta2)
        }
    }
    fun printAll(){
        val string = stringifyContent()
        printAll(string)
    }
}

class OutputBlock(
    private val name: NameHeader,
    private val setVerbosity: Verbosity = Verbosity.Debug
):OutputBase<Unit>(name){

    constructor(name:String,  verbosity: Verbosity = Verbosity.Debug):this(NameHeader(name), verbosity)

    val verbosity: Verbosity get() = setVerbosity

    private val useOutput =  Output(name, null, null)

    fun Any?.output(styleCode: StyleCode? = null){
       checkDispatcher()
       Output(null, this, styleCode).setModifier(Prefix(SpecialChars.NEW_LINE)).printAll()
    }

    fun Any?.output(topic: Topic,  styleCode: StyleCode? = null){
        if(verbosity.minTopicReached(topic)){
            checkDispatcher()
            val headerText = parameterToText(name, false)
            headerText.printNotEmpty()
            val content = getText(this, styleCode)
            val param = StringModifyParams(Prefix(SpecialChars.WHITESPACE), Postfix(SpecialChars.NEW_LINE))
            content.printNotEmpty(param)
            Output(null, this, styleCode).setModifier(Prefix(SpecialChars.NEW_LINE)).printAll()
        }
    }

    fun Any?.output(prefix: String? = null, styleCode: StyleCode? = null){
       val headerText = prefix?.let {
            parameterToText(NameHeader(it, TextStyler.ActionTag.Info), false)
       }
       headerText.printNotEmpty()
       val content = getText(this, styleCode)
       val param = StringModifyParams(Prefix(SpecialChars.WHITESPACE), Postfix(SpecialChars.NEW_LINE))
       content.printNotEmpty(param)
    }

    fun Any?.output(vararg parameters : OutputParameter) {
        checkDispatcher()
        val headerModifier = StringModifyParams(Separator(), Prefix(), Postfix(SpecialChars.WHITESPACE))
        val contentModifier = StringModifyParams(Separator(), Prefix(), Postfix())
        val output = Output(null, this).setModifier(headerModifier).setModifier(contentModifier, header = false).setParameters(parameters)
        output.printAll()
    }

    fun printAll(){
        val headerText = getParametersText()
        print(headerText)
    }
}

fun Any?.output(prefix: String? = null, styleCode: StyleCode? = null){
    checkDispatcher()
    val header = prefix?.let { NameHeader(it) }
    Output(header, this, styleCode).printAll()
}

fun Any?.output(vararg parameters : OutputParameter) {
    checkDispatcher()
    Output(null, this).setParameters(parameters).printAll()
}

fun Any?.output(styleCode: StyleCode){
    output(prefix = null, styleCode = styleCode)
}

fun <T> T.outputCompare(other:T, prefix:String? = null){
    checkDispatcher()
    val header = prefix?.let { NameHeader(it) }
    OutputCompare(header, this, other).printAll()
}

internal fun Any?.output(verbosity: Verbosity, styleCode: StyleCode? = null){
    if(verbosity == Verbosity.Debug){
        output(prefix = null, styleCode)
    }
    return
}

fun <R> Named.outputBlock(block: OutputBlock.()-> R):R {
    val verbosity = when(val receiver = this){
       is NamedComponent -> receiver.verbosity
       else -> Verbosity.Warnings
   }
   return block.invoke(OutputBlock(name, verbosity))
}

fun <R> outputBlock(name: String, block: OutputBlock.()-> R):R {
    checkDispatcher()
    val output = OutputBlock(name)
    output.printAll()
    return block.invoke(output)
}

inline fun <reified T: Any> T.output(prefix: String,  noinline configAction: ListOptions.(T) -> Unit){
     val options = ListOptions()
     options.header = prefix
     configAction.invoke(options, this)
     val styled = stringification(this, options)
     println(styled.toString())
}
