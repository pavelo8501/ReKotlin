package po.misc.data.styles

import po.misc.data.styles.Colour.RESET


fun String.colorize(colour: Colour): String{
    return "${colour.code}$this${RESET.code}"
}

fun  <T: Any> T.colorize(colour: Colour, textBuilder: (T)-> Any): String{
    val result = Colour.makeOfColour(colour, textBuilder(this).toString())
    return result
}


fun String.colorize(bgColour: BGColour): String{
    val result = BGColour.makeOfColour(bgColour, this)
    return result
}

fun <T: Any> T.colorize(bgColour: BGColour, textBuilder: (T)-> Any): String{
    val result = BGColour.makeOfColour(bgColour, textBuilder(this).toString())
    return result
}


fun String.colorize(bgColour: BGColour, colour: Colour): String{
    val result = BGColour.makeOfColour(bgColour, colour, this)
    return result
}

inline fun <T: Any> T.colorize(bgColour: BGColour, colour: Colour, textBuilder: (T)-> Any): String{
    val result = BGColour.makeOfColour(bgColour,colour,  textBuilder(this).toString())
    return result
}

infix fun Colour.text(message: String): String{
    return Colour.makeOfColour(this, message)
}
