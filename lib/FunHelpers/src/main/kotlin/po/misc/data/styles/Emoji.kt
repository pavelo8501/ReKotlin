package po.misc.data.styles

enum class Emoji(val symbol: String) {
    NONE(""),
    FIRE("🔥"),
    CHECK("✅"),
    CROSS("❌"),
    INFO("ℹ️"),
    WARNING("⚠️"),
    STAR("⭐"),
    SPARKLES("✨"),
    BUG("🐞"),
    CONSTRUCTION("🚧"),
    CLIPBOARD("📋"),
    ROCKET("🚀"),
    TOOLS("🛠️"),
    HOURGLASS("⏳"),
    LOCK("🔒"),
    UNLOCK("🔓"),
    LIGHTBULB("💡"),
    HAMMER("🔨"),
    HammerAndPick("⚒️"),
    MAGNET("🧲"),
    PENCIL("✏️"),
    NOTEBOOK("📒"),
    EYES("👀"),
    PARTY("🥳"),
    RED_CIRCLE("🔴"),
    GREEN_CIRCLE("🟢"),
    BLUE_CIRCLE("🔵"),
    BLACK_CIRCLE("⚫"),
    WHITE_CIRCLE("⚪");

    override fun toString(): String {
        return symbol
    }

    companion object {
        fun fromValue(symbol: String): Emoji {
            entries.firstOrNull { it.symbol == symbol }?.let {
                return it
            }
            return NONE
        }
    }

}