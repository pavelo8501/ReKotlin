package po.misc.data.styles


enum class Colour(val code: String) {
    Default(""),
    Red("\u001B[31m"),
    Yellow("\u001B[33m"),
    Green("\u001B[32m"),
    Gray("\u001B[90m"),
    Blue("\u001B[34m"),
    Magenta("\u001B[35m"),
    Cyan("\u001B[36m"),
    White("\u001B[37m"),
    BlackBright("\u001B[90m"),
    RedBright("\u001B[91m"),
    GreenBright("\u001B[92m"),
    YellowBright("\u001B[93m"),
    BlueBright("\u001B[94m"),
    MagentaBright("\u001B[95m"),
    CyanBright("\u001B[96m"),
    WhiteBright("\u001B[97m"),
    GrayLight("\u001B[90m"),
    RESET("\u001B[0m");

}