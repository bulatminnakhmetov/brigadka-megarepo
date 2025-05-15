package com.brigadka.app.presentation.common

fun getYearsPostfix(age: Int): String {
    val lastTwoDigits = age % 100
    val lastDigit = age % 10

    return if (lastTwoDigits in 11..14) {
        "лет"
    } else {
        when (lastDigit) {
            1 -> "год"
            2, 3, 4 -> "года"
            else -> "лет"
        }
    }
}

fun getProfilesPostfix(count: Int): String {
    val lastTwoDigits = count % 100
    val lastDigit = count % 10

    return if (lastTwoDigits in 11..14) {
        "профилей"
    } else {
        when (lastDigit) {
            1 -> "профиль"
            2, 3, 4 -> "профиля"
            else -> "профилей"
        }
    }
}