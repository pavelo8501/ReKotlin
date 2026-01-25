package po.misc.data.styles



enum class ColourStyle(val pallet: ColourPallet): StyleCode  {

    Default( ColourPallet(Colour.Default, BGColour.Default)),
    Selected(ColourPallet(Colour.BlackBright, BGColour.White)),
    Focused(ColourPallet(Colour.WhiteBright, BGColour.Blue)),

    ErrorStrong(ColourPallet(Colour.WhiteBright, BGColour.Red)),
    WarningStrong(ColourPallet(Colour.BlackBright, BGColour.Yellow)),
    SuccessStrong(ColourPallet(Colour.BlackBright, BGColour.Green)),
    InfoStrong(ColourPallet(Colour.BlackBright, BGColour.Cyan)),

    SuccessSoft(ColourPallet(Colour.GreenBright, BGColour.Default)),
    WarningSoft(ColourPallet(Colour.YellowBright, BGColour.Default)),
    ErrorSoft(ColourPallet(Colour.RedBright, BGColour.Default)),
    InfoSoft(ColourPallet(Colour.CyanBright, BGColour.Default)),

    Hover(ColourPallet(Colour.BlackBright, BGColour.Cyan)),
    Active(ColourPallet(Colour.WhiteBright, BGColour.Magenta)),

    Header(ColourPallet(Colour.WhiteBright, BGColour.Black)),
    Subtle(ColourPallet(Colour.GrayLight, BGColour.Default)),
    Disabled(ColourPallet(Colour.Gray, BGColour.Default)),

    RESET(ColourPallet(Colour.RESET, BGColour.RESET));

    override val code: String get() = pallet.code
}

data class ColourPallet(
    private val color: Colour,
    private val bgColour: BGColour,
):StyleCode{

    override val name: String = "ColourPallet"
    override val ordinal: Int get() = color.ordinal + bgColour.ordinal
    override val code: String get() {
        if(color == Colour.Default){
          return  SpecialChars.EMPTY
        }
        if(color == Colour.RESET){
           return Colour.RESET.code
        }
        return "${bgColour.code}${color.code}"
    }
}

data class StyleTheme(
    private val style: TextStyle,
    private val color: Colour,
    private val bgColour: BGColour,
):StyleCode{


    override val name: String = "StyleTheme"

    internal constructor(color: Colour, bgColour: BGColour):this(TextStyle.Regular, color, bgColour)

    override val ordinal: Int get() =  style.ordinal + color.ordinal + bgColour.ordinal
    override val code: String get() {
        var styleCode =  ""
        if(style != TextStyle.Regular){
            styleCode += style.code
        }
        if(color != Colour.Default){
            styleCode += color.code
        }
        if(bgColour != BGColour.Default){
            styleCode += color.code
        }
        return styleCode
    }
}
