package com.example.uts.utils

fun String.replaceNewlineWithSpace(): String {
    return this.replace("\n", " ")
}

fun maxCharacter(text: String, max: Int): String {
    return if (text.length > max) {
        text.substring(0, max) + "..."
    } else {
        text
    }
}