package app.frontend

import dev.fritz2.dom.html.renderElement
import dev.fritz2.dom.mount
import dev.fritz2.routing.router
import kotlinx.coroutines.ExperimentalCoroutinesApi

const val roomParam = "room"
const val nameParam = "name"
val defaultRoute = mapOf<String, String>()
val router = router(defaultRoute)

@ExperimentalCoroutinesApi
fun main() {
    renderElement {
        div("container clearfix") {
            router.data.render { params ->
                if(params[roomParam].isNullOrBlank() || params[nameParam].isNullOrBlank())
                    startPage()
                else chatPage(params[roomParam]!!, params[nameParam]!!)
            }
        }
    }.mount("target")
}