package po.misc.data.styles


fun String.colorize(colour: Colour): String{
    val result = Colour.makeOfColour(colour, this)
    return result
}

infix fun Colour.text(message: String): String{
    return Colour.makeOfColour(this, message)
}
