package com.batid.app.data.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "",
    val program: String = "",
    val yearLevel: String = "",
    val authorId: String = "",
    val createdAt: Long = 0L
)