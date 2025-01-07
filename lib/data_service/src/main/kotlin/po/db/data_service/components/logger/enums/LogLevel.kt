package po.db.data_service.components.logger.enums

enum class LogLevel (val level: Int) {
    MESSAGE(1),
    ACTION(2),
    WARNING(3),
    EXCEPTION(4);

    companion object {
        fun fromValue(level: Int): LogLevel? {
            LogLevel.entries.firstOrNull { it.level == level }?.let {
                return it
            }
            return MESSAGE
        }
    }
}