package app.backend

import app.model.ChatMessage
import app.model.ChatMessageResource
import app.model.MessageType
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class ChatClient(val member: String, val session: DefaultWebSocketSession)

private val chatroom = Collections.synchronizedMap(HashMap<String, MutableList<ChatClient>>())

fun Application.main() {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")

    install(WebSockets)

    routing {
        get("/") {
            call.resolveResource("index.html")?.let {
                call.respond(it)
            }
        }

        static("/") {
            resources("/")
        }

        webSocket("/chat/{id}") {
            val id = call.parameters["id"]
            if (id == null) call.respond(HttpStatusCode.BadRequest, "error: must provide id!")
            val session = this
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            // parse new message
                            val text = frame.readText()
                            val msg = ChatMessageResource.deserialize(text)

                            // get all clients in chatroom
                            val clients = chatroom[id] ?: mutableListOf()

                            // add new member to chatroom
                            if (clients.none { it.member == msg.member }) {
                                environment.log.debug("Add new member ${msg.member} to chatroom $id")
                                // inform other members that a new user is joined
                                clients.forEach {
                                    it.session.outgoing.send(Frame.Text(
                                        ChatMessageResource.serialize(joinMessage(msg.member))
                                    ))
                                }
                                clients.add(ChatClient(msg.member, session))
                            }

                            // broadcast message to other members
                            clients.filter { it.member != msg.member }.forEach {
                                environment.log.debug("Sending message from ${msg.member} to members ${it.member} in chatroom $id")
                                it.session.outgoing.send(Frame.Text(text))
                            }

                            // save new client list
                            chatroom[id] = clients
                        }
                        else -> call.respond(HttpStatusCode.BadRequest, "error: must provide json!")
                    }
                }
            } finally {
                // remove client from chatroom when session is closed
                chatroom[id]?.let { clients ->
                    clients.find { it.session == session }?.let { client ->
                        environment.log.debug("Remove member ${client.member} from chatroom $id")
                        clients.remove(client)
                        clients.forEach {
                            it.session.outgoing.send(Frame.Text(
                                ChatMessageResource.serialize(leaveMessage(client.member))
                            ))
                        }
                        chatroom[id] = clients
                    }
                }
            }
        }

        get("/members/{id}") {
            val id = call.parameters["id"]
            if (id == null) call.respond(HttpStatusCode.BadRequest, "error: must provide id!")
            else {
                val clients = chatroom[id]
                if(clients == null) call.respond(HttpStatusCode.BadRequest, "error: chatroom not found!")
                else call.respond(Json.encodeToString(ListSerializer(String.serializer()), clients.map { it.member }))
            }
        }
    }
}

fun joinMessage(member: String): ChatMessage = ChatMessage("New member $member joined the chat.", member, MessageType.JOINING)

fun leaveMessage(member: String): ChatMessage = ChatMessage("Member $member left the chat.", member, MessageType.LEAVING)

fun main(args: Array<String>): Unit = EngineMain.main(args)