package po.misc.data.styles


interface Colorizer {


    fun String.applyColour(colour: Colour): String{
        return Colour.applyColour(this, colour)
    }

}