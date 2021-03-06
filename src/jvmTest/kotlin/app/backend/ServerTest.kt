package app.backend

import app.shared.ChatMessage
import app.shared.MessageType
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServerTest {

    @Test
    fun `Test Chat with 2 Members`() {
        withTestApplication(Application::main) {
            val chatroom = "chat1"
            val memberA = "A"
            val msgA = "Hello, I'm A!"
            val memberB = "B"
            val msgB = "Hello, I'm B!"

            fun checkMembers(expected: List<String>) {
                handleRequest(HttpMethod.Get, "members/$chatroom").apply {
                    assertEquals(
                        expected,
                        Json.decodeFromString(ListSerializer(String.serializer()), response.content ?: "[1]")
                    )
                    assertEquals(200, response.status()?.value)
                }
            }


            handleWebSocketConversation("chat/$chatroom") { incomingA, outgoingA ->
                environment.log.info("Session A open")
                delay(200)

                handleWebSocketConversation("chat/$chatroom") { incomingB, outgoingB ->
                    environment.log.info("Session B open")

                    launch {
                        var count = 0
                        for (frame in incomingA) {
                            when (frame) {
                                is Frame.Text -> {
                                    val message = ChatMessage.fromJson(frame.readText())
                                    environment.log.info("Session A: ${message.content}")
                                    when (count++) {
                                        0 -> {
                                            assertEquals(MessageType.JOINING, message.type)
                                            assertEquals(memberB, message.member)
                                        }
                                        1 -> assertEquals(msgB, message.content)
                                        2 -> {
                                            assertEquals(MessageType.LEAVING, message.type)
                                            assertEquals(memberB, message.member)
                                        }
                                        else -> fail("Session A: unexpected message: $message")
                                    }
                                }
                                else -> fail("sessionA: Fame was not text")
                            }
                        }
                    }

                    launch {
                        for (frame in incomingB) {
                            when (frame) {
                                is Frame.Text -> {
                                    val message = ChatMessage.fromJson(frame.readText())
                                    environment.log.info("Session B: ${message.content}")
                                    assertEquals(msgA, message.content)
                                }
                                else -> fail("Session B: Fame was not text")
                            }
                        }
                    }

                    launch {
                        delay(500)
                        outgoingA.send(Frame.Text(ChatMessage("", memberA, MessageType.JOINING).toJson()))
                        delay(200)
                        outgoingB.send(Frame.Text(ChatMessage("", memberB, MessageType.JOINING).toJson()))

                        delay(200)
                        outgoingA.send(Frame.Text(ChatMessage(msgA, memberA).toJson()))
                        delay(200)
                        outgoingB.send(Frame.Text(ChatMessage(msgB, memberB).toJson()))
                        delay(200)
                        outgoingA.send(Frame.Text(ChatMessage(msgA, memberA).toJson()))
                        delay(500)
                    }
                    delay(2000)
                    checkMembers(listOf(memberA, memberB))
                }
                delay(200)
                checkMembers(listOf(memberA))
            }
            // check if room is closed
            handleRequest(HttpMethod.Get, "members/$chatroom").apply {
                assertEquals(400, response.status()?.value)
            }
        }
    }
}
