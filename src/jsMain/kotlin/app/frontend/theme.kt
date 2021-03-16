package app.frontend

import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.FlexParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.theme.AppFrameStyles
import dev.fritz2.styling.theme.DefaultTheme

object ChatTheme : DefaultTheme() {

    override val appFrame: AppFrameStyles = object : AppFrameStyles by super.appFrame {

        override val main: Style<BasicParams> = {
            padding { none }
            background { color { gray50 } }
            color { "rgb(44, 49, 54)" }
        }

        override val tabs: Style<FlexParams> = {
            borders {
                top {
                    width { "1px " }
                    style { solid }
                    color { gray200 }
                }
            }
            padding { tiny }
            children(" > button") {
                flex {
                    grow { "1" }
                    shrink { "1" }
                    basis { auto }
                }
                radius { none }
                height { full }
                padding { none }
            }

        }
    }
}