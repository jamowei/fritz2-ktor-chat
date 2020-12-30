package app.frontend

import app.model.ChatMessage
import app.model.MessageType
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.remote.body
import dev.fritz2.remote.getBody
import dev.fritz2.remote.http
import dev.fritz2.remote.websocket
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import dev.fritz2.binding.invoke
import dev.fritz2.components.icon
import dev.fritz2.components.inputField
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.styling.params.shadow
import dev.fritz2.styling.params.styled

fun RenderContext.renderMsg(msg: ChatMessage, self: Boolean) {
    val bgColor = if (self) "#94c2ed" else "#86bb71"

    lineUp({
        margins { bottom { normal } }
        textAlign { if (self) right else left }
        fontSize { small }
    }) {
        reverse { self }
        spacing { smaller }
        items {
            if (!self) {
                icon({
                    size { normal }
                    margins { top { "0.1rem" } }
                }) { fromTheme { user } }
            }
            (::span.styled {
                fontWeight { semiBold }
            }) {
                +msg.member
            }
            (::span.styled {
                color { "#a8aab1" }
            }) {
                icon({
                    size { small }
                    margins { right { tiny } }
                }) { fromTheme { clock } }
                +msg.created.toString()
            }
        }
    }
    (::div.styled {
        boxShadow { flat }
        background { color { bgColor } }
        color { "white" }
        paddings {
            vertical { smaller }
            horizontal { small }
        }
        lineHeight { huge }
        fontSize { normal }
        margins { bottom { larger } }
        width { "90%" }
        position { relative { } }
        margins {
            if (self) left { "10%" } else right { "10%"}
        }
        radius { normal }
        after {
            border {
                style { solid }
                width { "10px" }
                color { "transparent" }
            }
            borders { bottom { color { bgColor } } }
            position { absolute {
                bottom { "100%" }
                if (self) right { large } else left { large }
            } }
            height { none }
            width { none }
            css("""
                content: " ";
                pointer-events: none;                
            """.trimIndent())
        }
    }) {
        +msg.content
    }
}

fun RenderContext.chatPage(room: String, name: String) {

    val membersStore = object : RootStore<List<String>>(emptyList()) {
        val remote = http("/members/$room")

        val load = handle {
            Json.decodeFromString(ListSerializer(String.serializer()), remote.get().getBody()) - name
        }

        init { load() }
    }

    val messagesStore = object : RootStore<List<ChatMessage>>(emptyList()) {
        val session = websocket("ws://localhost:8080/chat/$room").connect()

        val receive = handle<ChatMessage> { msgs, msg ->
            when (msg.type) {
                MessageType.JOINING, MessageType.LEAVING -> membersStore.load()
            }
            msgs + msg
        }

        val send = handle<ChatMessage> { msgs, msg ->
            console.info("send: $msg")
            session.send(msg.toJson())
            msgs + msg
        }

        // join chat room
        val join = handle { msgs ->
            console.info("joining room: $name")
            session.send(ChatMessage("", name, MessageType.JOINING).toJson())
            msgs
        }

        init {
            join()
            session.messages.body.map { ChatMessage.fromJson(it) } handledBy receive
        }
    }

    lineUp {
        items {
            // members list
            (::div.styled {
                width { "260px" }
            }) {
                (::div.styled {
                    fontSize { large }
                    color { "white" }
                    fontWeight { bold }
                    margin { large }
                }) { + "Members" }
                (::ul.styled {
                    css("list-style: none;")
                    padding { large }
                    height { "770px" }
                }) {
                    membersStore.data.renderEach {
                        (::li.styled {
                            paddings { bottom { large } }
                        }) {
                            lineUp {
                                spacing { small }
                                items {
                                    img {
                                        src("https://s3-us-west-2.amazonaws.com/s.cdpn.io/195612/chat_avatar_01.jpg")
                                        alt("avatar")
                                    }
                                    (::div.styled {
                                        margins { top { tiny } }
                                    }) {
                                        (::div.styled {
                                            fontSize { normal }
                                            color { "white" }
                                            fontWeight { semiBold }
                                        }) { +it }
                                        (::div.styled {
                                            margins { top { "-0.25rem" } }
                                            fontSize { small }
                                            color { "#92959e" }
                                        }) {
                                            icon({
                                                size { small }
                                                margins { right { tiny } }
                                            }) { fromTheme { cloud }}
                                            +"online"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // chat
            (::div.styled {
                width { "490px" }
                background { color { "#f2f5f8" } }
                radii {
                    topRight { normal }
                    bottomRight { normal }
                }
                color { "#434651" }
            }) {
                (::div.styled {
                    padding { large }
                    borders {
                        bottom {
                            width { "2px" }
                            style { solid }
                            color { "white" }
                        }
                    }
                }) {
                    lineUp {
                        items {
                            img {
                                src("https://s3-us-west-2.amazonaws.com/s.cdpn.io/195612/chat_avatar_01_green.jpg")
                                alt("avatar")
                            }
                            stackUp {
                                spacing { none }
                                items {
                                    (::div.styled {
                                        fontWeight { bold }
                                        fontSize { large }
                                    }) {
                                        +"$name in $room"
                                    }
                                    (::div.styled {
                                        color { "#92959e" }
                                    }) {
                                        + "already "
                                        messagesStore.data.map { it.size }.asText()
                                        + " messages."
                                    }
                                }
                            }
                        }
                    }
                }
                (::div.styled {
                    paddings {
                        right { larger }
                    }
                    borders {
                        bottom {
                            width { "2px" }
                            style { solid }
                            color { "white" }
                        }
                    }
                    overflowY { scroll }
                    height { "575px" }
                }) {
                    (::ul.styled {
                        css("list-style: none;")
                    }) {
                        messagesStore.data.renderEach { msg ->
                            (::li.styled {

                            }) {
                                renderMsg(msg, (msg.member == name))
                            }
                        }
                    }
                }
                (::div.styled {
                    padding { larger }
                }) {
                    inputField({
                        width { full }
                    }) {
                        base {
                            keyups.key()
                                .filter { it.isKey(Keys.Enter) }
                                .map { ChatMessage(domNode.value, name).also { domNode.value = "" } }
                                .handledBy(messagesStore.send)
                        }
                    }
                }
            }
        }
    }
}