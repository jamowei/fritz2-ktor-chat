package app.frontend

import dev.fritz2.styling.params.ColorProperty
import dev.fritz2.styling.theme.Colors
import dev.fritz2.styling.theme.DefaultTheme

object ChatTheme : DefaultTheme() {

    override val colors: Colors = object : Colors by super.colors {
        override val primary: ColorProperty = "#023047"
        override val secondary: ColorProperty = "#FB8500"
        override val dark: ColorProperty = "#222222"
    }
}