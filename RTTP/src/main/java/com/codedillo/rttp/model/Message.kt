package com.codedillo.rttp.model

data class Message(
    var senderId: String = "",
    var recipientId: String = "",
    var topic: String = "",
    var action: Action = Action.Unknown,
    var payload: Value = Value()
): RObject() {

    enum class Action(val value: Int) {
        Get(0xF0),
        Set(0xF1),
        Update(0xF2),
        Delete(0xF3),
        Info(0xF4),
        Unknown(0xFF);

        companion object {
            fun of(value: Int): Action {
                return values().firstOrNull { it.value == value } ?: Unknown
            }
        }
    }

    override val data: List<Value>
        get() = listOf(Value(senderId), Value(recipientId), Value(topic), Value(action.value), payload)

    override fun assign(list: List<Value>) {
        if (list.size != 5) {
            isValid = false
            return
        }

        if (!list[0].isString() || !list[1].isString() || !list[2].isString() || !list[3].isNumber()) {
            isValid = false
            return
        }

        senderId = list[0].toString()
        recipientId = list[1].toString()
        topic = list[2].toString()
        action = Action.of(list[3].toInt())
        payload = list[4]
    }

    fun isEmpty():Boolean {
        return senderId.isEmpty() && recipientId.isEmpty() && topic.isEmpty()
    }

    companion object {
        const val SERVER_ID = "RTTP_SERVER"
        const val CHANNELS_TOPIC = "_channels"
        const val SUBSCRIBERS_TOPIC = "_subscribers"
        const val ALL_RECIPIENTS = "*"
        const val ALL_TOPICS = "*"
    }

}
