package app.frontend

import dev.fritz2.lenses.Lenses

@Lenses
data class JoinInfo(val room: String, val member: String) {
    companion object {
        val initial = JoinInfo("", "")
    }
}
