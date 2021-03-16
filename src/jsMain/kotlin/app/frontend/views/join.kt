package app.frontend.views

import app.frontend.ChatStore
import dev.fritz2.binding.Store
import dev.fritz2.components.*
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.params.styled
import kotlinx.coroutines.flow.map

fun RenderContext.joinPage(roomStore: Store<String>, memberStore: Store<String>) {

    stackUp({
        width { full }
        padding { large }
        alignItems { center }
    }) {
        spacing { large }
        items {

            (::img.styled {
                width(sm = { full }, lg = { "50%" } )
            }) {
                src("img/undraw_Status_update_re_dm9y.svg")
            }

            formControl {
                inputField(id = "name", store = roomStore) {
                    placeholder("Enter a title for your chat")
                }
            }

            formControl {
                inputField(id = "name", store = memberStore) {
                    placeholder("Enter your name")
                }
            }

            clickButton({
            }) {
                text("Join room")
                icon { fromTheme { message } }
            } handledBy ChatStore.join
        }
    }
}
