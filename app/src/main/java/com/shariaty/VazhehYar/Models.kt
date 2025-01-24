package com.shariaty.VazhehYar

data class PartsData(
    val parts: List<Part>
)

data class Part(
    val name: String,
    val levels: List<Level>
)

data class Level(
    val level: Int,
    val words: List<String>
)

