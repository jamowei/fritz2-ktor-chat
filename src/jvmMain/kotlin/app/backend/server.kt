package app.backend

import app.model.ChatMember
import app.model.ChatMessage
import app.model.MessageType
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.*

class ChatClient(val member: ChatMember, val session: DefaultWebSocketSession)

fun Application.main() {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets)

    routing {
        get("/") {
            call.respondRedirect("/index.html", permanent = true)
        }
        static("/") {
            resources("/")
        }

        val chatroom = Collections.synchronizedMap(HashMap<String, MutableList<ChatClient>>())

        webSocket("/chat/{id}") {
            val id = call.parameters["id"]
            if (id == null) call.respond(HttpStatusCode.BadRequest, "error: must provide id!")
            val session = this
            val clients: MutableList<ChatClient> = chatroom[id] ?: mutableListOf()
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val msg = ChatMessage.resourceSerializer.read(text)
                            // add new member to chatroom
                            if (clients.none { it.member == msg.member }) {
                                environment.log.info("Add new member (${msg.member.name}) to chatroom ($id)")
                                // inform other members that a new user is joined
                                clients.forEach {
                                    it.session.outgoing.send(Frame.Text(
                                        ChatMessage.resourceSerializer.write(joinMessage(it.member.name))
                                    ))
                                }
                                clients.add(ChatClient(msg.member, session))
                            }
                            // broadcast message to other members
                            clients.filter { it.member != msg.member }.forEach {
                                environment.log.info("Sending message to member (${it.member.name}) in chatroom ($id)")
                                it.session.outgoing.send(Frame.Text(text))
                            }
                            chatroom[id] = clients
                        }
                        else -> call.respond(HttpStatusCode.BadRequest, "error: must provide json!")
                    }
                }
            } finally {
                // remove client from chatroom when session is closed
                clients.find { it.session == session }?.let { client ->
                    environment.log.info("Remove member (${client.member.name}) from chatroom ($id)")
                    clients.remove(client)
                    clients.forEach {
                        it.session.outgoing.send(Frame.Text(
                            ChatMessage.resourceSerializer.write(leaveMessage(it.member.name))
                        ))
                    }
                    chatroom[id] = clients
                }
            }
        }

        webSocket("/members/{id}") {
            val id = call.parameters["id"]
            if (id == null) call.respond(HttpStatusCode.BadRequest, "error: must provide id!")
            while (true) {
                // sending current member list to client
                chatroom[id]?.let { clients ->
                    val json = ChatMember.resourceSerializer.writeList(clients.map { it.member })
                    outgoing.send(Frame.Text(json))
                }
                // delay for receiving new member list
                delay(500)
            }
        }
    }
}

val admin = ChatMember("admin")

fun joinMessage(name: String): ChatMessage = ChatMessage("New member $name joined the chat.", admin, MessageType.JOINING)

fun leaveMessage(name: String): ChatMessage = ChatMessage("Member $name left the chat.", admin, MessageType.LEAVING)

fun main(args: Array<String>): Unit = EngineMain.main(args)