package app.frontend

import app.frontend.views.joinPage
import app.shared.ChatMessage
import app.shared.L
import dev.fritz2.components.*
import dev.fritz2.components.icon
import dev.fritz2.dom.values
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.theme.DefaultTheme
import dev.fritz2.styling.theme.render
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import app.frontend.views.member
import app.frontend.views.members
import dev.fritz2.styling.style
import navSection

@ExperimentalCoroutinesApi
fun main() {
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
                }) { +"Where you are"}
            }

            nav {
                navSection("Members")
                ChatStore.data.render { chat  ->
                    if (chat.inRoom()) members(ChatStore.sub(L.Chat.members))
                }
            }

            footer {
                ChatStore.data.render { chat ->
                    if (chat.member.isNotBlank()) member(chat.member)
                }
            }

            main {
                ChatStore.data.render { chat  ->
                    if (chat.inRoom()) {
                        ul {
                            ChatStore.sub(L.Chat.messages).data.render {
                                it.forEach {
                                    li { +it.content }
                                }
                            }
                        }
                    }
                    else if (chat.room.isNotBlank())
                        joinPage(chat.room)
                    else
                        joinPage()
                }
            }

            tabs {
                ChatStore.data.render { chat  ->
                    if (chat.inRoom()) {
                        inputField {
                            events {
                                changes.values().map {
                                    ChatMessage(it, chat.member)
                                } handledBy ChatStore.send
                            }
                        }
                    }
                }
            }
        }
    }
}

