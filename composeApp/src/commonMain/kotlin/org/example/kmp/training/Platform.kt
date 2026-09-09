package org.example.kmp.training

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform