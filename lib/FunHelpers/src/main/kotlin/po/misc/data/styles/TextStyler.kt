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
    }
}

fun style(text: String, style: TextStyle): String = TextStyler.style(text, style)