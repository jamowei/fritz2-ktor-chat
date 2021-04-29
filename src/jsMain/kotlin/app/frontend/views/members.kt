package app.frontend.views

import dev.fritz2.binding.Store
import dev.fritz2.components.box
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.img

fun RenderContext.avatar(name: String) {
    img({
        radius { "50%" }
        background { color { primary.main } }
        padding { "3px" }
    }) {
        src("http://www.avatarpro.biz/avatar/${name.hashCode()})?s=55")
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
                box({
                    color { tertiary.mainContrast }
                    fontWeight { semiBold }
                    lineHeight { small }
                }) { +name }
                box({
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
