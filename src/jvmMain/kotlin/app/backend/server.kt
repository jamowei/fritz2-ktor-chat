package app.backend

import app.shared.ChatMessage
import app.shared.MessageType
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

        webSocket("/chat/{room}") {
            val room = call.parameters["room"]
            if (room == null) call.respond(HttpStatusCode.BadRequest, "error: must provide id!")
            val session = this
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            // parse new message
                            val text = frame.readText()
                            val msg = ChatMessage.fromJson(text)

                            // get all clients in chatroom
                            val clients = chatroom[room] ?: mutableListOf()

                            // add new member to chatroom
                            when(msg.type) {
                                MessageType.JOINING -> {
                                    environment.log.info("Add new member ${msg.member} to chatroom $room")
                                    // inform other members that a new user is joined
                                    clients.forEach {
                                        it.session.outgoing.send(
                                            Frame.Text(joinMessage(msg.member).toJson())
                                        )
                                    }
                                    clients.add(ChatClient(msg.member, session))
                                }
                                MessageType.MESSAGE -> {
                                    // broadcast message to other members
                                    clients.filter { it.member != msg.member }.forEach {
                                        environment.log.info("Sending message from ${msg.member} to members ${it.member} in chatroom $room")
                                        it.session.outgoing.send(Frame.Text(text))
                                    }
                                }
                                else -> call.respond(HttpStatusCode.BadRequest, "error: wrong message type!")
                            }

                            // save new client list
                            chatroom[room] = clients
                        }
                        else -> call.respond(HttpStatusCode.BadRequest, "error: must provide json!")
                    }
                }
            } finally {
                // remove client from chatroom when session is closed
                chatroom[room]?.let { clients ->
                    clients.find { it.session == session }?.let { client ->
                        environment.log.info("Remove member ${client.member} from chatroom $room")
                        clients.remove(client)
                        clients.forEach {
                            it.session.outgoing.send(Frame.Text(leaveMessage(client.member).toJson()))
                        }
                        if(clients.isEmpty()) chatroom.remove(room)
                        else chatroom[room] = clients
                    }
                }
            }
        }

        get("/members/{room}") {
            val room = call.parameters["room"]
            if (room == null) call.respond(HttpStatusCode.BadRequest, "error: must provide id!")
            else {
                val clients = chatroom[room]
                if(clients == null) call.respond(HttpStatusCode.BadRequest, "error: chatroom not found!")
                else call.respond(Json.encodeToString(ListSerializer(String.serializer()), clients.map { it.member }))
            }
        }

        get("/rooms") {
            call.respond(chatroom.size.toString())
        }
    }
}

fun joinMessage(member: String): ChatMessage = ChatMessage("New member $member joined the chat.", member, MessageType.JOINING)

fun leaveMessage(member: String): ChatMessage = ChatMessage("Member $member left the chat.", member, MessageType.LEAVING)

fun main(args: Array<String>): Unit = EngineMain.main(args)