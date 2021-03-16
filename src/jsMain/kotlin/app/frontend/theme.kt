package app.frontend

import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.ColorProperty
import dev.fritz2.styling.params.FlexParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.theme.AppFrameStyles
import dev.fritz2.styling.theme.Colors
import dev.fritz2.styling.theme.DefaultTheme

object ChatTheme : DefaultTheme() {

    override val colors: Colors = object : Colors by super.colors {
        override val primary: ColorProperty = "#023047"
        override val secondary: ColorProperty = "#FB8500"
        override val dark: ColorProperty = "#222222"
    }

    override val appFrame: AppFrameStyles = object : AppFrameStyles by super.appFrame {


        override val main: Style<BasicParams> = {
            padding { none }
            background { color { lightestGray } }
            color { "rgb(44, 49, 54)" }
        }

        override val tabs: Style<FlexParams> = {
            borders {
                top {
                    width { "1px " }
                    style { solid }
                    color { lighterGray }
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