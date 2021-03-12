package app.frontend

import app.frontend.views.chatPage
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
                ChatStore.data.render { chat ->
                    (::span.styled {
                        fontWeight { semiBold }
                        fontSize { large }
                    }) { +(if(chat.inRoom()) "${chat.member} @ ${chat.room}" else "Welcome!") }
                }
            }

            nav {
                ChatStore.data.render { chat  ->
                    if (chat.inRoom()) {
                        navSection("Members")
                        members(ChatStore.sub(L.Chat.members))
                    }
                }
            }

            footer {
                ChatStore.data.render { chat ->
                    if (chat.member.isNotBlank()) member(chat.member)
                }
            }

            main {
                ChatStore.data.render { chat  ->
                    if (chat.inRoom()) chatPage()
                    else joinPage()
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

