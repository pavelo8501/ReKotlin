package po.misc.data.styles

interface TextStyler {

    companion object {
        fun style(text: String, style: TextStyle): String{
            return "${style.code}$text${Colour.RESET.code}"
        }

        fun style(text: String, style: TextStyle, colour: Colour): String{
            return "${colour.code}${style.code}$text${Colour.RESET.code}"
        }

        fun style(
            text: String,
            style: TextStyle? = null,
            color: Colour? = null,
            background: BGColour? = null
        ): String {
            val codes = buildString {
                if (style != null) append(style.code)
                if (color != null) append(color.code)
                if (background != null) append(background.code)
            }
            return if (codes.isEmpty()) text else "$codes$text${Colour.RESET.code}"
        }


        fun style(
            text: String,
            applyColourIfExists: Boolean,
            style: TextStyle = TextStyle.Regular,
            colour: Colour? = null,
            background: BGColour? = null
        ): String {

            val colorized = if (colour != null) {
                Colorizer.applyColour(text, colour, background, applyColourIfExists)
            } else if (background != null) {
                Colorizer.applyColour(text, background, applyColourIfExists)
            } else {
                text
            }
            return "${style.code}$colorized"
        }


        fun style(
            text: String,
            style: TextStyle = TextStyle.Regular,
            color: Colour,
            applyColourIfExists: Boolean = false
        ): String {
            val text =  Colorizer.applyColour(text, color, applyColourIfExists)
            return "${style.code}$text"
        }

        fun style(
            text: String,
            style: TextStyle = TextStyle.Regular,
            color: Colour,
            background: BGColour,
            applyColourIfExists: Boolean = false
        ): String {
            val text =  Colorizer.applyColour(text, color, background, applyColourIfExists)
            return "${style.code}$text"
        }
    }
}

fun style(text: String, style: TextStyle): String = TextStyler.style(text, style)