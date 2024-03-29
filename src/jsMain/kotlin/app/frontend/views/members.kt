package app.frontend.views

import dev.fritz2.binding.Store
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import dev.fritz2.styling.img
import kotlin.math.abs

fun RenderContext.avatar(name: String) {
    val num = name.hashCode().toString().substring(0..1).let {
        val value = it.toInt()
        if (value > 70) 70 else abs(value)
    }

    img({
        radius { "50%" }
        background { color { primary.main } }
        padding { "3px" }
    }) {
        src("https://i.pravatar.cc/40?img=$num")
        alt("avatar")
    }
}

fun RenderContext.member(name: String): Div =
    lineUp({
        alignItems { center }
    }) {
        spacing { small }
        items {
            avatar(name)
            div {
                div({
                    color { tertiary.mainContrast }
                    fontWeight { semiBold }
                    lineHeight { small }
                }) { +name }
                div({
                    fontSize { small }
                    color { primary.main }
                }) {
                    icon({
                        size { small }
                        margins { right { tiny } }
                    }) { fromTheme { cloud } }
                    +"online"
                }
            }
        }
    }


fun RenderContext.members(store: Store<List<String>>) {
    stackUp({
        padding { small }
    }) {
        items {
            store.data.renderEach { name ->
                member(name)
            }
        }
    }
}
