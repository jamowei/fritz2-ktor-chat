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

            lineUp({
                width { full }
                padding { huge }
                background { color { lightestGray } }
                boxShadow { flat }
                radius { normal }
                alignItems { center }
            }) {
                items {
                    icon({ size { giant } }) { fromTheme { fritz2 } }
                    (::span.styled {
                        fontWeight { bold }
                        fontSize { large }
                    }) {
                        roomStore.data.map {
                            if (it.isNotBlank()) "Join chat room $it" else "Create a chat room"
                        }.asText()
                    }
                }
            }

            formControl {
                inputField(id = "name", store = memberStore) {
                    placeholder("Enter your chat name")
                }
            }

            clickButton({
            }) {
                text(roomStore.data.map { if (it.isNotBlank()) "Join" else "Create" })
                icon { fromTheme { message } }
            } handledBy ChatStore.join
        }
    }
}
