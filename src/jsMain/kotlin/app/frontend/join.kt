package app.frontend

import dev.fritz2.binding.RootStore
import dev.fritz2.components.*
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.identification.uniqueId
import dev.fritz2.styling.params.styled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

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
                                        +if(room != null) "Join chat room" else "Create a chat room"
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
                        inputField(id = "name", store = joinStore) {
                            placeholder("Your name")
                            keyups.key()
                                .filter { it.isKey(Keys.Enter) }.map { }
                                .handledBy(joinStore.join)
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
        }
    }
}