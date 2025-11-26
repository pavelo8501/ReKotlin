package po.misc.data.styles

import po.misc.data.HasText
import po.misc.data.styles.Colour.RESET

fun String.colorize(colour: Colour): String = Colorizer.colour(text =  this, colour = colour)

fun String.colorize(bgColour: BGColour): String = Colorizer.colour(this, bgColour)
fun String.colorize(bgColour: BGColour, colour: Colour): String = Colorizer.colour(this, colour, bgColour)

fun String.applyColour(colour: Colour): String = Colorizer.applyColour(this, colour)

inline fun <T: Any> T.colorize(
    bgColour: BGColour,
    colour: Colour,
    textBuilder: (T)-> Any
): String = Colorizer.colour(textBuilder(this).toString(), colour, bgColour)



fun  <T: Any> T.colorize(
    colour: Colour,
    textBuilder: (T)-> Any
): String = Colorizer.colour(textBuilder(this).toString(), colour)

fun  String.colorizeIf(colour: Colour, negativeCaseColour: Colour? = null,   predicate: ()-> Boolean): String{
    return  if(predicate.invoke()){
        Colorizer.colour(this, colour)
    }else{
        negativeCaseColour?.let {
            Colorizer.colour(this, it)
        }?:this
    }
}

fun  HasText.colorizeIf(
    colour: Colour,
    negativeCaseColour: Colour? = null,
    predicate: ()-> Boolean
): String = asText().colorizeIf(colour, negativeCaseColour, predicate)

fun  String.applyColourIf(colour: Colour, predicate: ()-> Boolean): String = colorizeIf(colour, predicate =  predicate)

infix fun Colour.text(message: String): String = Colorizer.colour(message, this)
