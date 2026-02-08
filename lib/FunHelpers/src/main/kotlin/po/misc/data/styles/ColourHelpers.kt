package po.misc.data.styles


import po.misc.interfaces.named.HasText



fun String.colorize(bgColour: BGColour, colour: Colour): String = TextStyler.ansi.style(this, colour, bgColour)

fun String.applyColour(colour: Colour): String = TextStyler.ansi.applyStyleCode(this,  colour)

inline fun <T: Any> T.colorize(
    bgColour: BGColour,
    colour: Colour,
    textBuilder: (T)-> Any
): String = TextStyler.ansi.style(textBuilder(this).toString(), colour, bgColour)


fun  <T: Any> T.colorize(
    colour: Colour,
    textBuilder: (T)-> Any
): String = TextStyler.ansi.style(textBuilder(this).toString(), colour)

fun  String.colorizeIf(colour: Colour, negativeCaseColour: Colour? = null,   predicate: ()-> Boolean): String{
    return  if(predicate.invoke()){
        TextStyler.ansi.style(this, colour)
    }else{
        negativeCaseColour?.let {
            TextStyler.ansi.style(this, it)
        }?:this
    }
}

fun HasText.colorizeIf(
    colour: Colour,
    negativeCaseColour: Colour? = null,
    predicate: ()-> Boolean
): String = asText().colorizeIf(colour, negativeCaseColour, predicate)

fun  String.applyColourIf(colour: Colour, predicate: ()-> Boolean): String = colorizeIf(colour, predicate =  predicate)
infix fun Colour.text(message: String): String = TextStyler.ansi.style(message, this)
