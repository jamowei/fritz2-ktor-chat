package app.frontend.views

import app.frontend.ChatStore
import dev.fritz2.binding.Store
import dev.fritz2.components.clickButton
import dev.fritz2.components.formControl
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.styling.img
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

fun RenderContext.joinPage(roomStore: Store<String>, memberStore: Store<String>) {

    stackUp({
        width { full }
        padding { large }
        alignItems { center }
    }) {
        spacing { large }
        items {

            img({
                width(sm = { full }, lg = { "50%" })
            }) {
                src("img/undraw_Status_update_re_dm9y.svg")
            }

            formControl {
                inputField(value = roomStore) {
                    placeholder("Enter the title of your chat")
                    events {
                        keyups.key().filter { it == Keys.Enter }.map {} handledBy ChatStore.join
                    }
                }
            }

            formControl {
                inputField(value = memberStore) {
                    placeholder("Enter your name")
                    events {
                        keyups.key().filter { it == Keys.Enter }.map {} handledBy ChatStore.join
                    }
                }
            }

            clickButton {
                text("Join room")
                icon { message }
            } handledBy ChatStore.join
        }
    }
}
