package app.frontend

import dev.fritz2.routing.router
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.theme.DefaultTheme
import dev.fritz2.styling.theme.render
import kotlinx.coroutines.ExperimentalCoroutinesApi

val defaultRoute = mapOf<String, String>()
val router = router(defaultRoute)

@ExperimentalCoroutinesApi
fun main() {
    render(DefaultTheme()) {
        (::div.styled {
            boxShadow { raised }
            margins {
                vertical { normal }
                horizontal { auto }
            }
            width { "750px" }
            background { color { "#ffffff" } }
            radius { normal }
        }) {
            router.data.render { params ->
                val room = params["room"]
                val member = params["member"]
                if(!room.isNullOrBlank() && !member.isNullOrBlank())
                    chatPage(room, member)
                else if(!room.isNullOrBlank())
                    joinPage(room)
                else
                    joinPage()
            }
        }
    }
}