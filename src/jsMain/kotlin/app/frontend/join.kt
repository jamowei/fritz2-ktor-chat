package app.frontend

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.watch
import dev.fritz2.components.*
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.identification.uniqueId
import dev.fritz2.styling.params.styled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLElement

@ExperimentalCoroutinesApi
fun RenderContext.joinPage(room: String? = null) {

    val joinStore = object : RootStore<String>("") {

        val join = handle {
            console.info("join chat room: $it")
            if (it.isNotBlank()) {
                router.navTo(
                    mapOf("room" to (room ?: uniqueId()), "member" to it.trim())
                )
                ""
            } else it
        }
    }

    fun RenderContext.joinTitle() {
        (::div.styled(prefix = "join-header") {
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
                                +if(room != null) "Join chat room" else "Create a chat room"
                            }
                        }
                    }
                }
            }
        }
    }

    fun RenderContext.joinContent() {
        (::div.styled(prefix = "join-content") {
            padding { larger }
        }) {
            formControl {
                label { "Your name" }
                inputField(id = "name", store = joinStore) {
                    placeholder("Enter your chat name")
                    keyups.key()
                        .filter { it.isKey(Keys.Enter) }.map { }
                        .handledBy(joinStore.join)
                    //sets focus after 300ms
                    delay(300) { domNode.focus() }
                }
                errorMessage {
                    joinStore.data.map {
                        if (it.isBlank()) "Please enter your name."
                        else ""
                    }
                }
            }
            flexBox({
                direction { row }
                justifyContent { center }
            }) {
                box {
                    clickButton({
                    }) {
                        text(if(room != null) "Join" else "Create")
                        icon { fromTheme { message } }
                    } handledBy joinStore.join
                }
            }
        }
    }

    lineUp {
        items {
            (::div.styled(prefix = "join") {
                width { "750px" }
                radii {
                    topRight { normal }
                    bottomRight { normal }
                }
                color { "#434651" }
            }) {
                joinTitle()
                joinContent()
            }
        }
    }
}

fun <E: HTMLElement> Tag<E>.delay(timeMillis: Long, consumer: (E) -> Unit) {
    flow<Unit> { delay(timeMillis) }.onEach { consumer(domNode) }.watch()
}