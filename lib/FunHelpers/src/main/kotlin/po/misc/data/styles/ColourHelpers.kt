package po.misc.data.styles


fun String.colorize(colour: Colour): String{
    return Colour.makeOfColour(colour, this)
}

infix fun Colour.text(message: String): String{
    return Colour.makeOfColour(this, message)
}
