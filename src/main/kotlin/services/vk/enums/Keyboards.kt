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
                                payload = "{\"command\":\"${Command.CHANGE}\"}"
                                label = "Изменить"
                            }
                        },

                        KeyboardButton().apply {
                            color = KeyboardButtonColor.NEGATIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.STOP}\"}"
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
                                payload = "{\"command\":\"${Command.START}\"}"
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
                                payload = "{\"command\":\"${Command.CONTINUE}\"}"
                                label = "Продолжить поиск"
                            }
                        },
                        KeyboardButton().apply
                        {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.CHANGE}\"}"
                                label = "Изменить параметры"
                            }
                        }
                    )
                )
            )
    ),

    NEXT(
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
                                payload = "{\"command\":\"${Command.NEXT}\"}"
                                label = "Пропустить"
                            }
                        }
                    )
                )
            )
    ),

    YAROSLAVL_DISTRICTS(
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
                                payload = "{\"command\":\"district_172\"}"
                                label = "Дзержинский"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"district_173\"}"
                                label = "Заволжский"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"district_174\"}"
                                label = "Кировский"
                            }
                        }
                    ),

                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"district_175\"}"
                                label = "Красноперекопский"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"district_176\"}"
                                label = "Ленинский"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"district_177\"}"
                                label = "Фрунзенский"
                            }
                        }
                    )
                )
            )
    )
}