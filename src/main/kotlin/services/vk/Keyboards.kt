package com.group.services.vk

import com.vk.api.sdk.objects.messages.*

enum class Keyboards(val keyboard: Keyboard) {
    Main(
        Keyboard()
            .setOneTime(true)
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
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
        Keyboard()
            .setInline(false)
            .setOneTime(true)
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