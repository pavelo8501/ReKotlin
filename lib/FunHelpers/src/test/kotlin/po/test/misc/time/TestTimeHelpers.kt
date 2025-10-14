package po.test.misc.time

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.time.TimeHelper
import java.time.Instant

class TestTimeHelpers: TimeHelper {


    @Test
    fun `Helpers time returned`(){

        nowTimeUtc().output("nowTimeUtc:", Colour.Cyan)
        nowTime(3).output("nowTime(hoursOffset=3):", Colour.Cyan)
        nowDateTimeUtc().output("nowDateTimeUtc:", Colour.Cyan)

        hoursFormated(Instant.now()).output("hoursFormated:", Colour.Cyan)
        hoursFormated(nowTime(3)).output("hoursFormated(nowTime(3)):", Colour.Cyan)
        dateFormated(nowLocalDateTime()).output("dateFormated(nowLocalDateTime()):", Colour.Cyan)
    }

}