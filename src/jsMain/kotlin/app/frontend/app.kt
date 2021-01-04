package app.frontend

import dev.fritz2.dom.mount
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
            height { "760px" }
            background { color { "#444753" } }
            radius { normal }
        }) {
            router.data.render { params ->
                if(params["room"].isNullOrBlank() || params["member"].isNullOrBlank())
                    joinPage()
                else chatPage(params["room"]!!, params["member"]!!)
            }
        }
    }.mount("target")
}