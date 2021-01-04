package app.frontend

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.invoke
import dev.fritz2.binding.watch
import dev.fritz2.components.*
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.remote.getBody
import dev.fritz2.remote.http
import dev.fritz2.styling.params.styled
import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
fun RenderContext.joinPage() {

    val joinStore = object : RootStore<JoinInfo>(JoinInfo.initial) {
        val remote = http("/rooms")

        val join = handle {
            console.info("join chat room: $it")
            if(JoinInfo.validator.isValid(it, Unit)) {
                window.open("/#room=${it.room.trim()}&member=${it.member.trim()}", target = "_blank")
                JoinInfo.initial
            } else it
        }

        val load = handle {
            val count = remote.get().getBody().toInt()
            it.copy(roomCount = count)
        }

        init { load() }
    }

    val roomStore = joinStore.sub(L.JoinInfo.room)
    val memberStore = joinStore.sub(L.JoinInfo.member)

    JoinInfo.validator.msgs.onEach { msgs ->
        msgs.onEach { window.alert(it.text) }
    }.watch()

    lineUp {
        items {
            (::div.styled {
                width { "750px" }
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
                    background { color { "#f2f5f8" } }
                }) {
                    lineUp({
                        alignItems { center }
                    }) {
                        items {
                            icon({ size { giant } }) { fromTheme { fritz2 } }
                            stackUp {
                                spacing { none }
                                items {
                                    (::div.styled {
                                        fontWeight { bold }
                                        fontSize { large }
                                    }) {
                                        +"Join or create a chat room"
                                    }
                                    (::div.styled {
                                        color { "#92959e" }
                                    }) {
                                        +"There are currently "
                                        joinStore.data.map { it.roomCount }.asText()
                                        +" chat rooms."
                                    }
                                }
                            }
                        }
                    }
                }

                (::div.styled {
                    padding { larger }
                }) {
                    formControl {
                        inputField(id = roomStore.id, store = roomStore) {
                            placeholder("Chat room name")
                        }
                    }
                    formControl {
                        inputField(id = memberStore.id, store = memberStore) {
                            placeholder("Your name")
                        }
                    }
                    flexBox({
                        direction { row }
                        justifyContent { center }
                    }) {
                        box {
                            clickButton({
                            }) {
                                text("Join")
                                icon { fromTheme { message } }
                            } handledBy joinStore.join
                        }
                    }
                }
            }
        }
    }
}