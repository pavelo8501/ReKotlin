package po.test.misc.data.styles

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.styles.BGColour
import po.misc.data.badges.Badge
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji
import kotlin.test.assertTrue

class TestBadge {

    @Test
    fun `Colorizing properly applied`(){
        val conf = Badge.Config
        assertTrue {
            conf.caption.contains("CONF")
        }
        conf.output()

        val custom = Badge.make("custom", Colour.BlackBright, BGColour.White)
        assertTrue {
            custom.caption.contains("CUSTOM")
        }
        custom.output()

        val emoji = Badge.make(Emoji.HOURGLASS, Colour.BlackBright, BGColour.White)
        emoji.output()
    }
}