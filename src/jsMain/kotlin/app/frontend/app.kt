package app.frontend

import dev.fritz2.dom.html.renderElement
import dev.fritz2.dom.mount
import dev.fritz2.routing.router
import dev.fritz2.styling.params.styled
import kotlinx.coroutines.ExperimentalCoroutinesApi

val defaultRoute = mapOf<String, String>()
val router = router(defaultRoute)

@ExperimentalCoroutinesApi
fun main() {
    renderElement {
        (::div.styled {
            css("""
                margin: 0 auto;
                width: 750px;
                background: #444753;
                border-radius: 5px;
            """.trimIndent())
        }) {
            router.data.render { params ->
                if(params["room"].isNullOrBlank() || params["member"].isNullOrBlank())
                    startPage()
                else chatPage(params["room"]!!, params["member"]!!)
            }
        }
    }.mount("target")
}