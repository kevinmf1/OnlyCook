package com.example.uts.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun dateConverter(date: Long): String {

    // Create a Date object from the timestamp
    val dateCreate = Date(date)

    // Create a date format
    val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    // Format the Date object to a readable date

    return format.format(dateCreate)
}