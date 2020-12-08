package app.backend

import app.database.database
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.jetbrains.exposed.sql.Database
import java.io.File

fun Application.main() {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets)

    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

    database {
//        SchemaUtils.create(...)
    }

    routing {
        get("/") {
            call.respondRedirect("/index.html", permanent = true)
        }
        static("/") {
            resources("/")
        }
        route("/api") {
            webSocket("/chat") {
                for (frame in incoming) {
                    try {
                        when (frame) {
                            is Frame.Text -> {
//                                val text = frame.readText()
//                                log.info("[ws-text] receiving: $text")
//                                outgoing.send(Frame.Text("Client said: $text"))
//                                if (text.equals("bye", ignoreCase = true)) {
//                                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
//                                }
                            }
                            is Frame.Close -> {
                                log.info("[ws-text] closing: ${closeReason.await()}")
                            }
                            else -> log.info(frame.frameType.name)
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        log.error("[ws-text] close: ${closeReason.await()}")
                    } catch (e: Throwable) {
                        log.error("[ws-text] error: ${closeReason.await()}")
                        throw e
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>): Unit = EngineMain.main(args)