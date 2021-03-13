package app.frontend

import app.frontend.views.chatPage
import app.frontend.views.joinPage
import app.frontend.views.member
import app.frontend.views.members
import app.shared.L
import dev.fritz2.components.appFrame
import dev.fritz2.components.icon
import dev.fritz2.components.inputField
import dev.fritz2.dom.values
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.theme.DefaultTheme
import dev.fritz2.styling.theme.render
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import navSection

fun main() {
    val roomStore = ChatStore.sub(L.Chat.room)
    val memberStore = ChatStore.sub(L.Chat.member)


    render(DefaultTheme()) {
        appFrame {
            brand {
                icon({
                    color { lighterGray }
                    size { "2rem" }
                    margins { right { normal } }
                }) { fromTheme { fritz2 } }
                (::span.styled {
                    fontWeight { semiBold }
                    fontSize { large }
                }) { +"Chat" }
            }

            header {
                (::span.styled {
                    fontWeight { semiBold }
                    fontSize { large }
                }) {
                    roomStore.data.combine(memberStore.data) { room, member ->
                        if (room.isBlank()) "Welcome!"
                        else "$member @ $room"
                    }.asText()
                }
            }

            nav {
                ChatStore.joined.render {
                    if (it) {
                        navSection("Members")
                        members(ChatStore.sub(L.Chat.members))
                    }
                }
            }

            footer {
                memberStore.data.render { member ->
                    if (member.isNotBlank()) member(member)
                }
            }

            main {
                ChatStore.joined.render {
                    if (it) chatPage() else joinPage(roomStore, memberStore)
                }
            }

            tabs {
                ChatStore.joined.render {
                    if (it) {
                        inputField {
                            events {
                                changes.values().map { value ->
                                    domNode.value = ""; value // resetting the value after sending
                                } handledBy ChatStore.send
                            }
                        }
                    }
                }
            }
        }
    }
}

