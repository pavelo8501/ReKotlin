package po.misc.data.styles

import po.misc.data.TextContaining

enum class Emoji(val symbol: String): TextContaining {

    NONE(""),
    FIRE("🔥"),
    CHECK("✅"),
    CROSS("❌"),
    EXCLAMATION("❗"),
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

    override fun asText(): String =  symbol
    override fun toString(): String = symbol


}