package app.frontend

import app.frontend.views.chatPage
import app.frontend.views.joinPage
import app.frontend.views.member
import app.frontend.views.members
import app.shared.L
import dev.fritz2.components.*
import dev.fritz2.dom.values
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.theme.render
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import navSection
import org.w3c.dom.HTMLInputElement

fun main() {
    val roomStore = ChatStore.sub(L.Chat.room)
    val memberStore = ChatStore.sub(L.Chat.member)

    render(ChatTheme) {
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

            actions {
                ChatStore.joined.render {
                    if (it) {
                        clickButton {
                            icon { fromTheme { share } }
                            variant { link }
                        } handledBy ChatStore.invite
                    }
                }
            }

            footer {
                ChatStore.joined.render {
                    if (it) {
                        memberStore.data.render { member ->
                            if (member.isNotBlank()) member(member)
                        }
                    }
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
                        lineUp({
                            width { full }
                        }) {
                            spacing { none }
                            items {
                                lateinit var inputFieldDomNode: HTMLInputElement
                                inputField {
                                    events {
                                        inputFieldDomNode = domNode
                                        changes.values().map { value ->
                                            domNode.value = "" // resetting the value after sending
                                            value
                                        } handledBy ChatStore.send
                                    }
                                }
                                clickButton {
                                    icon { fromTheme { send } }
                                    variant { ghost }
                                }.map { inputFieldDomNode.value } handledBy ChatStore.send
                            }
                        }
                    }
                }
            }
        }
    }
}

