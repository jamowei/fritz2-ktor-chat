package app.frontend

import dev.fritz2.identification.inspect
import dev.fritz2.lenses.Lenses
import dev.fritz2.validation.ValidationMessage
import dev.fritz2.validation.Validator

data class JoinValidationMessage(val id: String, val text: String) : ValidationMessage {
    override fun isError(): Boolean = true
}

@Lenses
data class JoinInfo(val room: String, val member: String, val roomCount: Int) {
    companion object {
        val initial = JoinInfo("", "", 0)
        val validator = object : Validator<JoinInfo, JoinValidationMessage, Unit>() {
            override fun validate(data: JoinInfo, metadata: Unit): List<JoinValidationMessage> {
                val msgs = mutableListOf<JoinValidationMessage>()
                val inspector = inspect(data)

                val room = inspector.sub(L.JoinInfo.room)
                if (room.data.isBlank())
                    msgs.add(JoinValidationMessage(room.id, "Please provide a room name."))

                val member = inspector.sub(L.JoinInfo.member)
                if(member.data.isBlank())
                    msgs.add(JoinValidationMessage(member.id, "Please provide a member name."))

                return msgs
            }
        }
    }
}
