package com.group.services.vk.enums

import com.group.datastore.entities.Districts
import com.vk.api.sdk.objects.messages.*

enum class Keyboards(val keyboard: Keyboard) {
    MAIN(
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

    START(
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
    ),

    Continue(
        Keyboard()
            .setInline(false)
            .setOneTime(true)
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply
                        {
                            color = KeyboardButtonColor.POSITIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"continue\"}"
                                label = "Продолжить"
                            }
                        },
                        KeyboardButton().apply
                        {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"change\"}"
                                label = "Изменить"
                            }
                        }
                    )
                )
            )
    ),

    SKIP(
        Keyboard()
            .setInline(false)
            .setOneTime(true)
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"skip\"}"
                                label = "Пропустить"
                            }

                        }
                    )
                )
            )
    ),

    DISTRICTS(
        Keyboard()
            .setInline(false)
            .setOneTime(true)
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"all_districts\"}"
                                label = "Все"
                            }

                        }
                    )
                )
            )
    )
}