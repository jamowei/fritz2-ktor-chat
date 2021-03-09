import dev.fritz2.binding.Store
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.params.styled

fun RenderContext.avatar(name: String) {
    (::img.styled {
        radius { "50%" }
        background { color { "#5eb97a" } }
        padding { "3px" }
    }) {
        src("http://www.avatarpro.biz/avatar/${name.hashCode()})?s=55")
        alt("avatar")
    }
}

fun RenderContext.member(name: String): Div =
    lineUp {
        spacing { small }
        items {
            avatar(name)
            (::div.styled {
                margins { top { tiny } }
            }) {
                (::div.styled {
                    fontSize { normal }
                    color { "white" }
                    fontWeight { semiBold }
                }) { +name }
                (::div.styled {
                    margins { top { "-0.25rem" } }
                    fontSize { small }
                    color { "#92959e" }
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
        padding { normal }
    }) {
        items {
            store.data.renderEach { name ->
                member(name)
            }
        }
    }
}
