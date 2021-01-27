package app.frontend

import dev.fritz2.binding.RootStore
import dev.fritz2.components.*
import dev.fritz2.components.validation.*
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.identification.inspect
import dev.fritz2.styling.params.styled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
fun RenderContext.joinPage(room: String? = null) {

    val joinStore = object : RootStore<String>(""), WithValidator<String, Unit> {

        override val validator = object : ComponentValidator<String, Unit>() {
            override fun validate(data: String, metadata: Unit): List<ComponentValidationMessage> {
                val name = inspect(data)
                return when {
                    name.data.isBlank() ->
                        listOf(errorMessage(name.id, "Please enter your name."))
                    name.data.trim().length > 25 ->
                        listOf(errorMessage(name.id, "Please use a shorter name."))
                    name.data.trim().length <= 3 ->
                        listOf(errorMessage(name.id, "Please use a longer name."))
                    else -> emptyList()
                }
            }
        }

        val join = handle {
            if (validator.isValid(it, Unit)) {
                console.info("join chat room: $it")
                router.navTo(
                    mapOf("room" to (room ?: randomId()), "member" to it.trim())
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
                                +if (room != null) "Join chat room $room" else "Create a chat room"
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
                errorMessage {
                    //FIXME: when internal component validation is available
                    joinStore.validationMessage()?.map { it?.message ?: "" } ?: flowOf("")
                }
                inputField(id = "name", store = joinStore) {
                    placeholder("Enter your chat name")
                    keyups.key()
                        .filter { it.isKey(Keys.Enter) }.map { }
                        .handledBy(joinStore.join)
                }
            }
            flexBox({
                direction { row }
                justifyContent { center }
            }) {
                box {
                    clickButton({
                    }) {
                        text(if (room != null) "Join" else "Create")
                        icon { fromTheme { message } }
                    } handledBy joinStore.join
                }
            }
        }
    }

    lineUp {
        items {
            (::div.styled(prefix = "container") {
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