package app.frontend

import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun main() {

    render {
        h1 { +"Hello World!" }
    }.mount("target")

}