package com.netraplus.data

data class ServiceItem(
    val id: String,
    val title: String,
    val description: String,
    val ttsMessage: String? = null
)
