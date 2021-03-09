package app.frontend

import app.shared.ChatMessage
import app.shared.L
import dev.fritz2.components.*
import dev.fritz2.components.icon
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.dom.values
import dev.fritz2.routing.router
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.theme.DefaultTheme
import dev.fritz2.styling.theme.render
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import member
import members
import navSection

val defaultRoute = mapOf<String, String>()
val router = router(defaultRoute)

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
                appContext.render { store -> members(store.sub(L.Chat.members)) }
            }

            footer {
                appContext.render { store -> member(store.name) }
            }

            main {
                appContext.render { chatStore ->
                    if (chatStore.inRoom) {
                        ul {
                            chatStore.sub(L.Chat.messages).data.render {
                                it.forEach {
                                    li { +it.content }
                                }
                            }
                        }
                    }
                    else if (chatStore.room.isNotBlank())
                        joinPage(chatStore.room)
                    else
                        joinPage()
                }
            }

            tabs {
                appContext.render { store ->
                    inputField {
                        events {
                            changes.values().map {
                                ChatMessage(it, store.name)
                            } handledBy store.send                        }
                    }
                }
            }
        }
    }
}

