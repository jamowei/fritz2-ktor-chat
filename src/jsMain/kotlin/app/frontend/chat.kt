package app.frontend

import app.shared.ChatMessage
import app.shared.MessageType
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.invoke
import dev.fritz2.components.*
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.remote.body
import dev.fritz2.remote.getBody
import dev.fritz2.remote.http
import dev.fritz2.remote.websocket
import dev.fritz2.styling.params.styled
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLTextAreaElement

@ExperimentalCoroutinesApi
fun RenderContext.chatPage(room: String, name: String) {

    document.title = "Chat $name | $room"

    val membersStore = object : RootStore<List<String>>(emptyList()) {
        val remote = http("/members/$room")

        val load = handle {
            delay(200)
            Json.decodeFromString(ListSerializer(String.serializer()), remote.get().getBody()) - name
        }

        val invite = handle {
            copyToClipboard(
                "${window.location.protocol}//${window.location.host}/#room=$room"
            )
            it
        }
    }

    val messagesStore = object : RootStore<List<ChatMessage>>(emptyList()) {
        val session = websocket("ws://${window.location.host}/chat/$room").connect()

        val receive = handle<ChatMessage> { msgs, msg ->
            if (msg.type == MessageType.JOINING || msg.type == MessageType.LEAVING) membersStore.load()
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
            membersStore.load()
        }
    }

    fun RenderContext.chatMessage() {
        (::div.styled(prefix = "chat-message") {
            height { "10%" }
            width { "100%" }
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

    fun RenderContext.chatContent() {
        (::div.styled(prefix = "chat-content") {
            height { "75%" }
            width { "100%" }
            paddings {
                right { larger }
            }
            background { color { "#ffffff" } }
            borders {
                bottom {
                    width { "1px" }
                    style { solid }
                    color { "#c7c7c7" }
                }
            }
            overflowY { scroll }
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
    }

    fun RenderContext.chatInvitation() {
        (::div.styled {
            css("float: right")
        }) {
            popover {
                trigger {
                    clickButton {
                        icon { fromTheme { share } }
                        text("Invite")
                    } handledBy membersStore.invite
                    placement { bottom }
                }
                content {
                    div { +"Invitation link copied to clipboard!" }
                }
            }
        }
    }

    fun RenderContext.chatTitle() {
        (::div.styled(prefix = "chat-header") {
            height { "15%" }
            width { "100%" }
            padding { large }
            borders {
                bottom {
                    width { "1px" }
                    style { solid }
                    color { "#c7c7c7" }
                }
            }
        }) {
            chatInvitation()
            lineUp {
                items {
                    avatar(name)
                    stackUp {
                        spacing { none }
                        items {
                            (::div.styled {
                                fontWeight { bold }
                                fontSize { large }
                            }) {
                                +name
                            }
                            (::div.styled {
                                color { "#92959e" }
                            }) {
                                messagesStore.data.map {
                                    if (it.isEmpty()) "no messages"
                                    else "${it.size} messages"
                                }.asText()
                            }
                        }
                    }
                }
            }
        }
    }

    fun RenderContext.chat() {
        stackUp({
            width { "70%" }
            height { "100%" }
            background { color { "#f2f5f8" } }
            color { "#434651" }
            radii {
                right { normal }
            }
        }, prefix = "chat") {
            spacing { none }
            items {
                chatTitle()
                chatContent()
                chatMessage()
            }
        }
    }

    fun RenderContext.members() {
        (::div.styled(prefix = "members") {
            width { "30%" }
        }) {
            (::div.styled {
                fontSize { large }
                color { "white" }
                fontWeight { bold }
                margin { large }
            }) { +"Members" }
            (::ul.styled {
                css("list-style: none;")
                padding { large }
                overflowY { auto }
            }) {
                membersStore.data.renderEach {
                    (::li.styled {
                        paddings { bottom { large } }
                    }) {
                        lineUp {
                            spacing { small }
                            items {
                                avatar(it)
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
                                        }) { fromTheme { cloud } }
                                        +"online"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    lineUp({
        width { "750px" }
        height { "770px" }
        background { color { "#444753" } }
        radius { normal }
    }, prefix = "container") {
        items {
            members()
            chat()
        }
    }
}

@ExperimentalCoroutinesApi
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
            if (self) left { "10%" } else right { "10%" }
        }
        radius { normal }
        after {
            border {
                style { solid }
                width { "10px" }
                color { "transparent" }
            }
            borders { bottom { color { bgColor } } }
            position {
                absolute {
                    bottom { "100%" }
                    if (self) right { large } else left { large }
                }
            }
            height { none }
            width { none }
            css(
                """
                content: " ";
                pointer-events: none;                
            """.trimIndent()
            )
        }
    }) {
        +msg.content
    }
}


@ExperimentalCoroutinesApi
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

fun copyToClipboard(text: String) {
    document.body?.let { body ->
        (document.createElement("textarea") as HTMLTextAreaElement).apply {
            value = text
            setAttribute("readonly", "")
            style.position = "absolute"
            style.left = "-9999px"
            body.appendChild(this)
            select()
            setSelectionRange(0, 999999)
            document.execCommand("copy")
            body.removeChild(this)
        }
    }
}