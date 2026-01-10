package po.misc.data.strings

import po.misc.context.CTX
import po.misc.data.HasText
import po.misc.data.HasValue
import po.misc.data.Named
import po.misc.data.PrettyFormatted
import po.misc.data.PrettyPrint
import po.misc.data.TextContaining
import po.misc.data.containsAnyOf
import po.misc.data.isUnset
import po.misc.data.output.output
import po.misc.data.strings.StringifyOptions
import po.misc.data.styles.Colour
import po.misc.data.styles.Colour.RESET
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.exceptions.throwableToText
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.reflect.KClass


//interface StringFormatter{
//
//    fun formatKnownTypes(receiver: Any?): FormattedText = Companion.formatKnownTypes(receiver)
//    companion object{
//        private val ANSI_REGEX = Regex("\\u001B\\[[;\\d]*m")
//        fun isStyled(text:String):Boolean {
//            return text.contains(RESET.code)
//        }
//        fun stripAnsiIfAny(text:String):String {
//            if(isStyled(text)){
//                stripAnsi(text)
//            }
//            return text
//        }
//        fun stripAnsi(text: String): String = text.replace(ANSI_REGEX, "")
//        fun hasStyles(text: String): Boolean{
//            return text.containsAnyOf(TextStyle.Reset.code)
//        }
//        fun style(text: String, styleCode: StyleCode?): String {
//            if(styleCode != null){
//                if(hasStyles(text)){
//                    stripAnsi(text)
//                }
//                return "${styleCode.code}$text${TextStyle.Reset.code}"
//            }else{
//                return text
//            }
//        }
//        fun formatKnownTypes(receiver: Any?): FormattedText {
//            return if(receiver != null){
//                val targetAsString = receiver.toString()
//                when(receiver){
//                    is KClass<*> -> {
//                        val info = ClassResolver.classInfo(receiver)
//                        FormattedText(info.simpleName, info.formattedClassName)
//                    }
//                    is PrettyFormatted -> {
//                        FormattedText(targetAsString).also {
//                            it.overflowPrevention = true
//                        }
//                    }
//                    is PrettyPrint -> {
//                        FormattedText(targetAsString, receiver.formattedString)
//                    }
//                    is CTX -> FormattedText(targetAsString,  receiver.identifiedByName)
//                    is Enum<*> -> {
//                        if(receiver is TextContaining){
//                            FormattedText(targetAsString, "${receiver.name}: ${receiver.asText()}")
//                        }else{
//                            FormattedText(targetAsString)
//                        }
//                    }
//                    is Throwable ->{
//                        FormattedText(receiver.message?:"", receiver.throwableToText())
//                    }
//                    is String -> FormattedText(targetAsString)
//                    is Boolean -> {
//                        if(receiver){
//                            FormattedText("true",  "True".colorize(Colour.Green))
//                        }else{
//                            FormattedText("false",  "False".colorize(Colour.Red))
//                        }
//                    }
//                    else -> FormattedText(targetAsString)
//                }
//            }else{
//                FormattedText("null", "null".colorize(Colour.Yellow))
//            }
//        }
//    }
//}