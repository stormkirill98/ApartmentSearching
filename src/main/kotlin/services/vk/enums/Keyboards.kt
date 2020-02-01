package com.group.services.vk.enums

import com.vk.api.sdk.objects.messages.*

enum class Keyboards(val keyboard: Keyboard) {
    MAIN(
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

    WAIT(
        Keyboard()
            .setInline(false)
            .setOneTime(true)
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply
                        {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.CHANGE}\"}"
                                label = "Изменить"
                            }
                        },
                        KeyboardButton().apply
                        {
                            color = KeyboardButtonColor.POSITIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.CONTINUE}\"}"
                                label = "Продолжить"
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
                                label = "Далее"
                            }
                        }
                    )
                )
            )
    ),

    YAROSLAVL_DISTRICTS(
        Keyboard()
            .setInline(false)
            .setOneTime(false)
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
                    ),

                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.NEGATIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.CLEAR}\"}"
                                label = "Сбросить"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.ALL}\"}"
                                label = "Любой"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.POSITIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.NEXT}\"}"
                                label = "Далее"
                            }
                        }
                    )
                )
            )
    ),

    COUNT_ROOMS(
        Keyboard()
            .setInline(false)
            .setOneTime(false)
            .setButtons(
                listOf(
                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${CountRoomCommand.ROOM_1}\"}"
                                label = "1"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${CountRoomCommand.ROOM_2}\"}"
                                label = "2"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${CountRoomCommand.ROOM_3}\"}"
                                label = "3"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${CountRoomCommand.ROOM_MORE_3}\"}"
                                label = "3+"
                            }
                        }
                    ),

                    listOf(
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.NEGATIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.CLEAR}\"}"
                                label = "Сбросить"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.PRIMARY
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.ALL}\"}"
                                label = "Любой"
                            }
                        },
                        KeyboardButton().apply {
                            color = KeyboardButtonColor.POSITIVE
                            action = KeyboardButtonAction().apply {
                                type = KeyboardButtonActionType.TEXT
                                payload = "{\"command\":\"${Command.NEXT}\"}"
                                label = "Далее"
                            }
                        }
                    )
                )
            )
    ),

    PRICE(
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
                                payload = "{\"command\":\"${Command.ALL}\"}"
                                label = "Любая"
                            }
                        }
                    )
                )
            )
    ),

    LANDLORDS(
        Keyboard()
        .setInline(false)
        .setOneTime(true)
        .setButtons(
            listOf(
                listOf(
                    KeyboardButton().apply {
                        color = KeyboardButtonColor.NEGATIVE
                        action = KeyboardButtonAction().apply {
                            type = KeyboardButtonActionType.TEXT
                            payload = "{\"command\":\"${LandlordCommand.ALL}\"}"
                            label = "Без разницы"
                        }
                    },
                    KeyboardButton().apply {
                        color = KeyboardButtonColor.PRIMARY
                        action = KeyboardButtonAction().apply {
                            type = KeyboardButtonActionType.TEXT
                            payload = "{\"command\":\"${LandlordCommand.ONLY_OWNER}\"}"
                            label = "Собственник"
                        }
                    }
                )
            )
        )
    ),

    CONFIRM(
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
                            payload = "{\"command\":\"${Command.CHANGE}\"}"
                            label = "Изменить"
                        }
                    },
                    KeyboardButton().apply {
                        color = KeyboardButtonColor.POSITIVE
                        action = KeyboardButtonAction().apply {
                            type = KeyboardButtonActionType.TEXT
                            payload = "{\"command\":\"${Command.START}\"}"
                            label = "Паконим"
                        }
                    }
                )
            )
        ))
}