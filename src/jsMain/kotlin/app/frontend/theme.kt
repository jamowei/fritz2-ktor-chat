package app.frontend

import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.FlexParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.theme.AppFrameStyles
import dev.fritz2.styling.theme.DefaultTheme

object ChatTheme : DefaultTheme() {

    override val appFrame: AppFrameStyles = object : AppFrameStyles by super.appFrame {

        override val brand: Style<FlexParams> = {
            background { color { tertiary.main } }
            paddings {
                all { small }
                left { normal }
            }
            color { tertiary.mainContrast }
            alignItems { center }
            borders {
                bottom {
                    width { "1px " }
                    color { gray400 }
                }
            }
        }

        override val sidebar: Style<BasicParams> = {
            background { color { tertiary.main } }
            color { tertiary.mainContrast }
            minWidth { "25vw" }
        }

        override val content: Style<BasicParams> = {
            padding { none }
            background { color { gray50 } }
            color { font }
        }

        override val tablist: Style<FlexParams> = {
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