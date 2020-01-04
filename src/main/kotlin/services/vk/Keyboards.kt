package com.group.services.vk

import com.vk.api.sdk.objects.messages.*

val keyboard: Keyboard = Keyboard()
    .setInline(false)
    .setOneTime(true)

enum class Keyboards(val keyboard: Keyboard) {
    Main(
        keyboard
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.DEFAULT
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"change\"}"
                                label = "Изменить"
                            }

                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.NEGATIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"stop\"}"
                                label = "Остановить"
                            }

                        }
                    )
                )
            )
    ),

    Start(
        keyboard
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.POSITIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"start\"}"
                                label = "Начать"
                            }

                        }
                    )
                )
            )
    )
}