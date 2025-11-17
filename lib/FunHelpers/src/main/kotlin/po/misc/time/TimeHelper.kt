package po.misc.time

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface TimeHelper {

    interface DateTimeFormats{
        val patten: String

        companion object{
            const val dateTime = "dd.MM.yyyy / HH:mm:ss"
        }
    }

    object DateTimeFormat : DateTimeFormats {
        override val patten: String = DateTimeFormats.dateTime
    }

    /**
     * Returns the current moment in UTC as an [Instant].
     *
     * Use when an absolute, timezone-independent timestamp is required.
     */
    fun nowTimeUtc(): Instant = Instant.now()

    /**
     * Returns the current date and time in UTC as a [ZonedDateTime].
     *
     * Useful for human-readable logging or storage while preserving timezone context.
     */
    fun nowDateTimeUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

    /**
     * Returns the current time with a fixed hour offset from UTC.
     *
     * @param hoursOffset Offset in hours from UTC. Positive or negative values are allowed.
     * @return A current timestamp as [OffsetDateTime] with the specified offset.
     */
    fun nowTime(hoursOffset: Int): OffsetDateTime = Instant.now().atOffset(ZoneOffset.ofHours(hoursOffset))

    /**
     * Returns the current local system date and time as [ZonedDateTime].
     *
     * This respects the system default timezone.
     */
    fun nowLocalDateTime(): ZonedDateTime = ZonedDateTime.now()

    /**
     * Formats an [Instant] to a time-only string using `HH:mm:ss` in UTC.
     *
     * @param instant The [Instant] to format.
     * @return A string representation of the time (UTC) in `HH:mm:ss` format.
     */
    fun hoursFormated(instant: Instant): String{
        return instant.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    /**
     * Formats an [OffsetDateTime] to a time-only string using `HH:mm:ss`.
     *
     * @param offsetTime The offset-aware datetime to format.
     * @return A string in `HH:mm:ss` format.
     */
    fun hoursFormated(offsetTime: OffsetDateTime): String{
        return offsetTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    fun Instant.toLocalZoneTime(
        hoursOffset: Int = 2
    ): ZonedDateTime {
       return atZone(ZoneOffset.ofHours(hoursOffset))
    }

    fun Instant.toLocalDateTime(
        format : DateTimeFormats = DateTimeFormat,
        hoursOffset: Int = 2
    ): String {
        return atZone(ZoneOffset.ofHours(hoursOffset)).timeFormatted(format)
    }

    fun Instant.toLocalTime(
        hoursOffset: Int = 2
    ): String {
        return atZone(ZoneOffset.ofHours(hoursOffset)).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    fun  ZonedDateTime.format(pattern : String): String{
        return format(DateTimeFormatter.ofPattern(pattern))
    }

    fun  ZonedDateTime.timeFormatted(format : DateTimeFormats): String{
        return format(DateTimeFormatter.ofPattern(format.patten))
    }

    fun Instant.hoursFormated(hoursOffset: Int): String =  hoursFormated(this.atOffset(ZoneOffset.ofHours(hoursOffset)))

    /**
     * Formats an [Instant] to a full date-time string using `dd-MM-yyyy / HH:mm:ss` in UTC.
     *
     * @param instant The [Instant] to format.
     * @return A full timestamp string (UTC) in `dd-MM-yyyy / HH:mm:ss` format.
     */
    fun dateFormated(instant: Instant): String{
      return instant.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd-MM-yyyy / HH:mm:ss"))
    }

    /**
     * Formats an [OffsetDateTime] to a full date-time string using `dd-MM-yyyy / HH:mm:ss`.
     *
     * @param offsetTime The offset-aware datetime to format.
     * @return A formatted string in `dd-MM-yyyy / HH:mm:ss` format.
     */
    fun dateFormated(offsetTime: OffsetDateTime): String{
        return offsetTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy / HH:mm:ss"))
    }

    /**
     * Formats a [ZonedDateTime] to a full date-time string using `dd-MM-yyyy / HH:mm:ss`.
     *
     * @param zonedDateTime The zoned datetime to format.
     * @return A formatted string in `dd-MM-yyyy / HH:mm:ss` format.
     */
    fun dateFormated(zonedDateTime: ZonedDateTime): String{
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy / HH:mm:ss"))
    }

    /**
     * Calculates the elapsed time between this [Instant] and another [Instant].
     *
     * @param nextTime The later moment to compare against.
     * @return The duration difference as [Duration].
     */
    fun Instant.elapsedSince(nextTime: Instant): Duration{
        return (nextTime.toEpochMilli() - toEpochMilli()).milliseconds
    }

    /**
     * Parses this string into an [Instant].
     *
     * @throws java.time.format.DateTimeParseException If the text cannot be parsed.
     */
    fun String.asInstant(): Instant{
        return Instant.parse(this)
    }

    /**
     * Creates a named [ExecutionTimeStamp] for measuring execution durations.
     *
     * @param name Identifier for this timestamp.
     * @return A new [ExecutionTimeStamp] instance.
     */
    fun createTimeStamp(name: String):ExecutionTimeStamp{
       return ExecutionTimeStamp(name)
    }

}